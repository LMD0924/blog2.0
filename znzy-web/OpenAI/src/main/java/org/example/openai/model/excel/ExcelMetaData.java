package org.example.openai.model.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/25
 * @Description:
 */

import lombok.Data;

import java.util.List;

/**
 * Excel元数据类
 */
@Data
public class ExcelMetaData {
    private String fileName;
    private int sheetCount;
    private List<SheetInfo> sheets;
}
