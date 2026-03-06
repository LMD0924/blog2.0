package org.example.openai.util.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.openai.model.excel.ExcelMetaData;
import org.example.openai.model.excel.SheetInfo;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * 通用Excel解析器
 * 支持任意格式的Excel文件解析，不依赖特定的表头结构
 *
 * @Author: 总会落叶
 * @Date: 2025/12/10
 */
@Slf4j
@Component
public class UniversalExcelParser {

    /**
     * 1. 智能解析：自动检测表头，返回结构化数据
     */
    public List<Map<String, Object>> parseToMapList(InputStream is, String fileName) throws IOException {
        return parseToMapList(is, fileName, 0);
    }

    /**
     * 指定Sheet索引的解析
     */
    public List<Map<String, Object>> parseToMapList(InputStream is, String fileName, int sheetIndex)
            throws IOException {
        return parseToMapList(is, fileName, sheetIndex, true, 0, -1);
    }

    /**
     * 完整参数的解析方法，支持所有解析选项
     */
    public List<Map<String, Object>> parseToMapList(InputStream is, String fileName, int sheetIndex,
                                                   boolean autoDetectHeader, int headerRowIndex, int dataStartRow)
            throws IOException {

        List<Map<String, Object>> result = new ArrayList<>();

        try (Workbook workbook = createWorkbook(is, fileName)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            // 确定表头行索引
            int finalHeaderRowIndex = autoDetectHeader ? detectHeaderRow(sheet) : headerRowIndex;
            Row headerRow = sheet.getRow(finalHeaderRowIndex);

            // 提取表头（处理重复、空值等问题）
            List<String> headers = extractHeaders(headerRow);

            // 确定数据开始行（将用户传入的行号转换为0-based索引）
            int finalDataStartRow = dataStartRow > 0 ? (dataStartRow - 1) : finalHeaderRowIndex + 1;

            // 解析数据行
            for (int i = finalDataStartRow; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null || isEmptyRow(dataRow)) {
                    continue;
                }

                Map<String, Object> rowData = parseRowWithHeaders(dataRow, headers);
                if (!rowData.isEmpty()) {
                    result.add(rowData);
                }
            }

            log.info("成功解析Excel文件: {}, Sheet: {}, 共{}行数据",
                    fileName, sheet.getSheetName(), result.size());
        }

        return result;
    }

    /**
     * 2. 流式解析（支持大数据量）
     */
    public void parseStreaming(InputStream is, String fileName, Consumer<Map<String, Object>> rowConsumer)
            throws IOException {

        try (Workbook workbook = createWorkbook(is, fileName)) {
            Sheet sheet = workbook.getSheetAt(0);

            int headerRowIndex = detectHeaderRow(sheet);
            Row headerRow = sheet.getRow(headerRowIndex);
            List<String> headers = extractHeaders(headerRow);

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null || isEmptyRow(dataRow)) {
                    continue;
                }

                Map<String, Object> rowData = parseRowWithHeaders(dataRow, headers);
                if (!rowData.isEmpty()) {
                    rowConsumer.accept(rowData);
                }
            }
        }
    }

    /**
     * 3. 自定义列映射解析
     */
    public List<Map<String, Object>> parseWithColumnMapping(InputStream is, String fileName,
                                                            Map<String, String> columnMapping)
            throws IOException {

        List<Map<String, Object>> result = new ArrayList<>();

        try (Workbook workbook = createWorkbook(is, fileName)) {
            Sheet sheet = workbook.getSheetAt(0);

            int headerRowIndex = detectHeaderRow(sheet);
            Row headerRow = sheet.getRow(headerRowIndex);

            // 构建Excel列索引到目标列名的映射
            Map<Integer, String> indexToTargetMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String excelHeader = getCellStringValue(cell);
                if (columnMapping.containsKey(excelHeader)) {
                    indexToTargetMap.put(cell.getColumnIndex(), columnMapping.get(excelHeader));
                }
            }

            // 解析数据
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null || isEmptyRow(dataRow)) {
                    continue;
                }

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> entry : indexToTargetMap.entrySet()) {
                    Cell cell = dataRow.getCell(entry.getKey(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowData.put(entry.getValue(), getUniversalCellValue(cell));
                }

                if (!rowData.isEmpty()) {
                    result.add(rowData);
                }
            }
        }

        return result;
    }

    /**
     * 4. 获取Excel元信息
     */
    public ExcelMetaData getExcelMetaData(InputStream is, String fileName) throws IOException {
        ExcelMetaData metaData = new ExcelMetaData();

        try (Workbook workbook = createWorkbook(is, fileName)) {
            metaData.setFileName(fileName);
            metaData.setSheetCount(workbook.getNumberOfSheets());

            List<SheetInfo> sheetInfos = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                SheetInfo sheetInfo = new SheetInfo();
                sheetInfo.setIndex(i);
                sheetInfo.setName(sheet.getSheetName());
                sheetInfo.setTotalRows(sheet.getLastRowNum() + 1);

                // 检测表头行
                int headerRowIndex = detectHeaderRow(sheet);
                sheetInfo.setHeaderRowIndex(headerRowIndex);

                if (headerRowIndex >= 0) {
                    Row headerRow = sheet.getRow(headerRowIndex);
                    sheetInfo.setHeaders(extractHeaders(headerRow));
                    sheetInfo.setDataRows(sheet.getLastRowNum() - headerRowIndex);
                }

                sheetInfos.add(sheetInfo);
            }

            metaData.setSheets(sheetInfos);
        }

        return metaData;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检测表头行
     */
    private int detectHeaderRow(Sheet sheet) {
        int maxNonEmptyCells = 0;
        int headerRowIndex = 0;

        // 检查前10行（通常表头在前几行）
        for (int i = 0; i < Math.min(10, sheet.getLastRowNum() + 1); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                int nonEmptyCells = 0;
                for (Cell cell : row) {
                    Object value = getUniversalCellValue(cell);
                    if (value != null && !value.toString().trim().isEmpty()) {
                        nonEmptyCells++;
                    }
                }

                if (nonEmptyCells > maxNonEmptyCells) {
                    maxNonEmptyCells = nonEmptyCells;
                    headerRowIndex = i;
                }
            }
        }

        return maxNonEmptyCells > 0 ? headerRowIndex : 0;
    }

    /**
     * 提取表头
     */
    private List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        if (headerRow == null) return headers;

        // 获取最大列索引
        int lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String header = getCellStringValue(cell);

            // 如果表头为空，生成默认列名
            if (header.trim().isEmpty()) {
                header = "Column_" + (i + 1);
            }

            // 处理重复列名
            String finalHeader = header;
            long duplicateCount = headers.stream()
                    .filter(h -> h.equals(finalHeader))
                    .count();
            if (duplicateCount > 0) {
                header = header + "_" + (duplicateCount + 1);
            }

            headers.add(header);
        }

        return headers;
    }

    /**
     * 解析单行数据
     */
    private Map<String, Object> parseRowWithHeaders(Row row, List<String> headers) {
        Map<String, Object> rowData = new LinkedHashMap<>();
        if (row == null || headers.isEmpty()) return rowData;

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            rowData.put(headers.get(i), getUniversalCellValue(cell));
        }

        return rowData;
    }

    /**
     * 检查是否为空行
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;

        for (Cell cell : row) {
            Object value = getUniversalCellValue(cell);
            if (value != null && !value.toString().trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 通用单元格值获取
     */
    private Object getUniversalCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();

        // 公式单元格需要计算值
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case STRING:
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : value;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue();
                }
                double num = cell.getNumericCellValue();
                // 判断是否为整数
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    long longValue = (long) num;
                    // 判断是否在整数范围内
                    if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                        return (int) longValue;
                    }
                    return longValue;
                }
                return num;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case BLANK:
                return null;
            case ERROR:
                return "ERROR: " + cell.getErrorCellValue();
            default:
                return null;
        }
    }

    private String getCellStringValue(Cell cell) {
        Object value = getUniversalCellValue(cell);
        return value != null ? value.toString() : "";
    }

    private Workbook createWorkbook(InputStream is, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        } else if (fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(is);
        }
        throw new IllegalArgumentException("不支持的文件格式: " + fileName);
    }
}
