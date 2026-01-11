package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2025/12/23
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateLog {
    private Integer id;//id
    private Integer userId;//用户id
    private String userName;//用户名
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operateTime;//操作时间
    private String className;//类名
    private String methodName;//方法名
    private Boolean success;           // 操作是否成功
    private String errorMessage;       // 错误信息
    private Long costTime;             // 耗时（毫秒）
    private String clientIp;           // 客户端IP
    private String requestUri;         // 请求URI
}
