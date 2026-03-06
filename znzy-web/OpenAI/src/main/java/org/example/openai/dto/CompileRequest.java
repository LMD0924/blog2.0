package org.example.openai.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description:
 */
@Data
public class CompileRequest {

    @NotBlank(message = "编程语言不能为空")
    private String language;

    @NotBlank(message = "代码不能为空")
    @Size(max = 10000, message = "代码长度不能超过10000字符")
    private String code;

    private String input;

    // 构造器
    public CompileRequest() {}

    public CompileRequest(String language, String code, String input) {
        this.language = language;
        this.code = code;
        this.input = input;
    }
}
