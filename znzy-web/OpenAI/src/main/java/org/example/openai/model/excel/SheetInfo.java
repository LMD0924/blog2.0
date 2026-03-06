package org.example.openai.model.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/25
 * @Description:
 */

import lombok.Data;

import java.util.List;

/**
 * Sheet信息类
 */
@Data
public class SheetInfo {
    private int index;
    private String name;
    private int totalRows;
    private int headerRowIndex;
    private int dataRows;
    private List<String> headers;
}
