package org.example.openai.model.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/25
 * @Description:
 */

import lombok.Data;

import java.util.Date;

/**
 * 数据验证规则
 */
@Data
public class ValidationRule {
    private String fieldName;
    private boolean required;
    private Integer maxLength;
    private String pattern; // 正则表达式
    private Class<?> dataType;
    private String errorMessage;

    public boolean validate(Object value) {
        // 必填验证
        if (required && (value == null || value.toString().trim().isEmpty())) {
            errorMessage = "不能为空";
            return false;
        }

        if (value == null) {
            return true; // 非必填字段为空是合法的
        }

        String strValue = value.toString().trim();

        // 长度验证
        if (maxLength != null && strValue.length() > maxLength) {
            errorMessage = "长度不能超过" + maxLength + "个字符";
            return false;
        }

        // 正则验证
        if (pattern != null && !strValue.matches(pattern)) {
            errorMessage = "格式不正确";
            return false;
        }

        // 类型验证
        if (dataType != null) {
            try {
                if (dataType == Integer.class) {
                    Integer.parseInt(strValue);
                } else if (dataType == Double.class) {
                    Double.parseDouble(strValue);
                } else if (dataType == Date.class) {
                    // 日期验证逻辑
                }
            } catch (Exception e) {
                errorMessage = "必须是" + dataType.getSimpleName() + "类型";
                return false;
            }
        }

        return true;
    }
}
