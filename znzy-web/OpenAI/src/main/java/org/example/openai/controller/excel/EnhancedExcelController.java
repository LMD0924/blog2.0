package org.example.openai.controller.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/25
 * @Description:
 */

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.openai.model.excel.ExcelImportResult;
import org.example.openai.model.excel.ExcelMetaData;
import org.example.openai.model.RestBean;
import org.example.openai.service.excel.EasyExcelService;
import org.example.openai.service.excel.EnhancedExcelParserService;
import org.example.openai.util.excel.UniversalExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 增强版Excel控制器
 */
@RestController
@RequestMapping("/api/excel")
public class EnhancedExcelController {

    @Autowired
    private EasyExcelService easyExcelService;

    @Autowired
    private EnhancedExcelParserService enhancedExcelParserService;

    @Autowired
    private UniversalExcelParser universalExcelParser;

    /**
     * 1. 通用Excel解析（支持任何格式）
     */
    @PostMapping("/parse/universal")
    public RestBean<ExcelImportResult<?>> parseUniversalExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parseMode", defaultValue = "universal") String parseMode,
            @RequestParam(value = "sheetIndex", defaultValue = "-1") int sheetIndex,
            @RequestParam(value = "autoDetectHeader", defaultValue = "true") boolean autoDetectHeader,
            @RequestParam(value = "headerRowIndex", defaultValue = "0") int headerRowIndex,
            @RequestParam(value = "dataStartRow", defaultValue = "-1") int dataStartRow) {

        try {
            if (file.isEmpty()) {
                return RestBean.failure("文件不能为空");
            }

            // 处理sheetIndex默认值，如果为-1则使用默认的0
            int finalSheetIndex = sheetIndex >= 0 ? sheetIndex : 0;
            
            // 通用解析，传递所有前端参数
            ExcelImportResult<Map<String, Object>> result = enhancedExcelParserService.parseAnyExcel(
                    file, finalSheetIndex, autoDetectHeader, headerRowIndex, dataStartRow);

            if (result.getSuccess()) {
                return RestBean.success("文件解析成功", result);
            } else {
                return RestBean.failure(result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("通用Excel解析失败"+ e);
            return RestBean.failure("解析失败: " + e.getMessage());
        }
    }

    /**
     * 2. 分Sheet解析
     */
    @PostMapping("/parse/multi-sheet")
    public RestBean<Map<String, ExcelImportResult<Map<String, Object>>>> parseMultiSheetExcel(
            @RequestParam("file") MultipartFile file) {

        try {
            Map<String, ExcelImportResult<Map<String, Object>>> results =
                    enhancedExcelParserService.parseMultiSheet(file);

            return RestBean.success("多Sheet解析完成", results);

        } catch (Exception e) {
            System.out.println("多Sheet解析失败"+e);
            return RestBean.failure("解析失败: " + e.getMessage());
        }
    }

    /**
     * 3. 获取Excel元信息
     */
    @PostMapping("/meta")
    public RestBean<ExcelMetaData> getExcelMetaData(@RequestParam("file") MultipartFile file) {
        try {
            ExcelMetaData metaData = universalExcelParser.getExcelMetaData(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            return RestBean.success("元数据获取成功", metaData);

        } catch (Exception e) {
            System.out.println("获取Excel元数据失败"+ e);
            return RestBean.failure("获取元数据失败: " + e.getMessage());
        }
    }

    /**
     * 4. 自定义列映射解析
     */
    @PostMapping("/parse/with-mapping")
    public RestBean<ExcelImportResult<?>> parseWithColumnMapping(
            @RequestParam("file") MultipartFile file,
            @RequestBody ColumnMappingRequest mappingRequest) {

        try {
            Map<String, String> columnMapping = mappingRequest.getColumnMapping();

            List<Map<String, Object>> dataList = universalExcelParser.parseWithColumnMapping(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    columnMapping
            );

            List<String> headers = new ArrayList<>(columnMapping.values());
            ExcelImportResult<Map<String, Object>> result = ExcelImportResult.success(dataList, headers);

            return RestBean.success("解析成功", result);

        } catch (Exception e) {
            System.out.println("自定义列映射解析失败"+ e);
            return RestBean.failure("解析失败: " + e.getMessage());
        }
    }

    /**
     * 5. 大数据量异步解析
     */
    @PostMapping("/parse/async")
    public RestBean<AsyncImportResponse> parseAsync(@RequestParam("file") MultipartFile file) {
        try {
            String taskId = UUID.randomUUID().toString();

            // 启动异步任务
            CompletableFuture.runAsync(() -> {
                try {
                    enhancedExcelParserService.parseLargeFile(file, 1000, page -> {
                        // 处理每一页数据
                        System.out.println("处理数据页，大小: {}"+ page.size());
                        // TODO: 保存到数据库或其他处理
                    });
                } catch (Exception e) {
                    System.out.println("异步解析失败"+ e);
                }
            });

            AsyncImportResponse response = new AsyncImportResponse();
            response.setTaskId(taskId);
            response.setStatus("PROCESSING");
            response.setMessage("任务已提交，正在处理中");

            return RestBean.success("异步解析任务已提交", response);

        } catch (Exception e) {
            System.out.println("提交异步任务失败"+ e);
            return RestBean.failure("提交任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 6. 下载Excel导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("用户数据");
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户名", "手机号", "邮箱", "部门", "职位", "入职日期"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                // 设置列宽
                sheet.setColumnWidth(i, 25 * 256); // 25个字符宽度
            }
            
            // 添加示例数据行
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {"张三", "13800138000", "zhangsan@example.com", "技术部", "工程师", "2023-01-01"};
            for (int i = 0; i < exampleData.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleData[i]);
            }
            
            // 设置响应头
            String fileName = "用户导入模板.xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            
            // 输出到响应流
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
            
            // 关闭工作簿
            workbook.close();
            
        } catch (IOException e) {
            System.out.println("模板下载失败" + e);
        }
    }

    // 内部类
    @Data
    public static class ColumnMappingRequest {
        private Map<String, String> columnMapping; // Excel列名 -> 目标列名
        private int sheetIndex = 0;
    }

    @Data
    public static class AsyncImportResponse {
        private String taskId;
        private String status;
        private String message;
        private int processedRows;
        private int totalRows;
    }
}
