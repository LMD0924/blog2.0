package org.example.openai.service.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/25
 * @Description:
 */
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.openai.model.excel.ExcelImportResult;
import org.example.openai.model.excel.ValidationRule;
import org.example.openai.util.excel.UniversalExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 增强版Excel解析服务
 *
 * @Author: 总会落叶
 * @Date: 2025/12/10
 */
@Service
public class EnhancedExcelParserService {

    @Autowired
    private UniversalExcelParser universalExcelParser;

    /**
     * 1. 通用解析方法 - 支持任何格式
     */
    public ExcelImportResult<Map<String, Object>> parseAnyExcel(MultipartFile file) {
        return parseAnyExcel(file, 0, true, 0, -1);
    }

    /**
     * 带参数的通用解析方法
     */
    public ExcelImportResult<Map<String, Object>> parseAnyExcel(MultipartFile file, int sheetIndex,
                                                               boolean autoDetectHeader, int headerRowIndex, int dataStartRow) {
        try {
            List<Map<String, Object>> dataList = universalExcelParser.parseToMapList(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    sheetIndex,
                    autoDetectHeader,
                    headerRowIndex,
                    dataStartRow
            );

            // 提取表头（使用第一行数据的key）
            List<String> headers = dataList.isEmpty() ?
                    new ArrayList<>() :
                    new ArrayList<>(dataList.get(0).keySet());

            return ExcelImportResult.success(dataList, headers);

        } catch (Exception e) {
            System.out.println("Excel解析失败"+ e);
            return ExcelImportResult.error("解析失败: " + e.getMessage());
        }
    }

    /**
     * 2. 分Sheet解析
     */
    public Map<String, ExcelImportResult<Map<String, Object>>> parseMultiSheet(MultipartFile file) {
        Map<String, ExcelImportResult<Map<String, Object>>> results = new LinkedHashMap<>();

        try (Workbook workbook = getWorkbook(file)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                try {
                    List<Map<String, Object>> dataList = universalExcelParser.parseToMapList(
                            file.getInputStream(),
                            file.getOriginalFilename(),
                            i
                    );

                    List<String> headers = dataList.isEmpty() ?
                            new ArrayList<>() :
                            new ArrayList<>(dataList.get(0).keySet());

                    results.put(sheetName, ExcelImportResult.success(dataList, headers));

                } catch (Exception e) {
                    results.put(sheetName, ExcelImportResult.error("Sheet解析失败: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            System.out.println("多Sheet解析失败"+ e);
        }

        return results;
    }

    /**
     * 3. 带数据验证的解析
     */
    public ExcelImportResult<Map<String, Object>> parseWithValidation(MultipartFile file,
                                                                      List<ValidationRule> validationRules) {
        ExcelImportResult<Map<String, Object>> result = parseAnyExcel(file);

        if (!result.getSuccess()) {
            return result;
        }

        List<Map<String, Object>> validatedData = new ArrayList<>();
        List<String> errors = result.getErrors();

        for (int i = 0; i < result.getData().size(); i++) {
            Map<String, Object> row = result.getData().get(i);
            boolean isValid = true;

            for (ValidationRule rule : validationRules) {
                Object value = row.get(rule.getFieldName());
                if (!rule.validate(value)) {
                    errors.add(String.format("第%d行: %s %s",
                            i + 2, // +2是因为Excel行号从1开始，且第1行是表头
                            rule.getFieldName(),
                            rule.getErrorMessage()
                    ));
                    isValid = false;
                }
            }

            if (isValid) {
                validatedData.add(row);
            }
        }

        result.setData(validatedData);
        result.setImportedRows(validatedData.size());
        return result;
    }

    /**
     * 4. 大数据量分页解析
     */
    public void parseLargeFile(MultipartFile file, int pageSize,
                               Consumer<List<Map<String, Object>>> pageConsumer) {
        try {
            List<Map<String, Object>> currentPage = new ArrayList<>();
            AtomicInteger rowCount = new AtomicInteger();

            universalExcelParser.parseStreaming(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    row -> {
                        currentPage.add(row);
                        rowCount.getAndIncrement();

                        if (currentPage.size() >= pageSize) {
                            pageConsumer.accept(new ArrayList<>(currentPage));
                            currentPage.clear();
                        }
                    }
            );

            // 处理最后一页
            if (!currentPage.isEmpty()) {
                pageConsumer.accept(currentPage);
            }

            System.out.println("大数据量解析完成，共{}行"+ rowCount);

        } catch (Exception e) {
            System.out.println("大数据量解析失败"+ e);
            throw new RuntimeException("解析失败", e);
        }
    }

    private Workbook getWorkbook(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();

        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("不支持的文件格式");
        }
    }
}

