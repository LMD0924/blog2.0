package org.example.openai.model.excel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel文件导入结果封装类
 * 用于统一返回Excel导入操作的结果信息，包括成功/失败状态、数据、错误信息等
 *
 * @param <T> 泛型参数，表示导入数据的实体类型
 * @author 总会落叶
 * @date 2025/12/6
 */
@Data
public class ExcelImportResult<T> {

    /**
     * 导入操作是否成功
     * true: 导入成功（可能有部分数据导入成功）
     * false: 导入失败（通常指整个导入流程失败）
     */
    private Boolean success;

    /**
     * 操作结果消息
     * 成功时可包含提示信息，失败时通常包含失败原因
     */
    private String message;

    /**
     * 成功导入的数据列表
     * 仅包含通过验证并成功处理的数据对象
     */
    private List<T> data;

    /**
     * Excel文件的列头信息
     * 用于记录原始Excel文件的列名，便于后续处理或展示
     */
    private List<String> columns;

    /**
     * Excel文件中的总行数（包含表头）
     * 注意：此处的行数通常指Excel中的物理行数，可能包含空行
     */
    private Integer totalRows;

    /**
     * 实际成功导入的行数
     * 即data列表的大小，排除表头、空行、验证失败的行
     */
    private Integer importedRows;

    /**
     * 导入过程中的错误信息列表
     * 每条错误信息通常包含：行号、列名、错误原因
     * 即使success=true，也可能包含部分数据的错误信息
     */
    private List<String> errors;

    /**
     * 创建成功的导入结果对象
     * 适用于全部或部分数据导入成功的场景
     *
     * @param <T> 泛型参数，表示导入数据的实体类型
     * @param data 成功导入的数据列表，不能为null
     * @param columns Excel文件的列头信息列表
     * @return 配置好的成功结果对象
     * @throws IllegalArgumentException 如果data为null
     */
    public static <T> ExcelImportResult<T> success(List<T> data, List<String> columns) {
        if (data == null) {
            throw new IllegalArgumentException("导入数据列表不能为null");
        }

        ExcelImportResult<T> result = new ExcelImportResult<>();
        result.setSuccess(true);
        result.setData(data);
        result.setColumns(columns != null ? columns : new ArrayList<>());
        result.setTotalRows(data.size()); // 这里假设totalRows指有效数据行数
        result.setImportedRows(data.size());
        result.setErrors(new ArrayList<>()); // 初始化空错误列表
        result.setMessage("导入成功，共导入 " + data.size() + " 条数据");
        return result;
    }

    /**
     * 创建失败的导入结果对象
     * 适用于整个导入流程失败的场景（如文件读取失败、模板不匹配等）
     *
     * @param <T> 泛型参数，表示导入数据的实体类型
     * @param message 失败原因描述，应清晰明确
     * @return 配置好的失败结果对象
     * @throws IllegalArgumentException 如果message为null或空字符串
     */
    public static <T> ExcelImportResult<T> error(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("错误信息不能为空");
        }

        ExcelImportResult<T> result = new ExcelImportResult<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setErrors(new ArrayList<>()); // 初始化空错误列表
        result.setData(new ArrayList<>()); // 失败时数据列表为空
        result.setColumns(new ArrayList<>()); // 失败时列头信息为空
        result.setTotalRows(0);
        result.setImportedRows(0);
        return result;
    }

    /**
     * 添加错误信息到错误列表
     * 用于记录单行数据的导入错误
     *
     * @param errorMsg 错误信息，格式建议为："第X行[列名]: 错误描述"
     */
    public void addError(String errorMsg) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        if (errorMsg != null && !errorMsg.trim().isEmpty()) {
            this.errors.add(errorMsg);
        }
    }

    /**
     * 检查是否存在错误信息
     *
     * @return true: 存在错误信息; false: 错误列表为空
     */
    public boolean hasErrors() {
        return this.errors != null && !this.errors.isEmpty();
    }

    /**
     * 获取错误数量
     *
     * @return 错误信息的数量
     */
    public int getErrorCount() {
        return this.errors != null ? this.errors.size() : 0;
    }
}