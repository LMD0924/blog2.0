package org.example.openai.dto;

/*
 * @Author:总会落叶
 * @Date:2025/12/10
 * @Description:
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompileResponse {

    private String status;          // success, error
    private String output;          // 程序输出
    private String error;           // 错误信息
    private Long executionTime;     // 执行时间(ms)
    private Long memoryUsage;       // 内存使用(KB)
    private String message;         // 附加消息

    // 成功响应
    public static CompileResponse success(String output) {
        return CompileResponse.builder()
                .status("success")
                .output(output)
                .build();
    }

    public static CompileResponse success(String output, Long executionTime) {
        return CompileResponse.builder()
                .status("success")
                .output(output)
                .executionTime(executionTime)
                .build();
    }

    // 错误响应
    public static CompileResponse error(String error) {
        return CompileResponse.builder()
                .status("error")
                .error(error)
                .build();
    }

    public static CompileResponse error(String error, String message) {
        return CompileResponse.builder()
                .status("error")
                .error(error)
                .message(message)
                .build();
    }
}
