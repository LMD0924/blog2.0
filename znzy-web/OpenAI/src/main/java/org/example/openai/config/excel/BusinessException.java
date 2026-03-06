package org.example.openai.config.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/6
 * @Description:
 */
import lombok.Getter;
import org.example.openai.model.RestBean;

/**
 * 业务异常类
 * <p>
 * 继承自RuntimeException，用于表示业务逻辑中的异常情况
 * 包含错误码和错误消息，用于统一的异常处理
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     * <p>
     * 对应HTTP状态码或自定义业务错误码
     * </p>
     */
    private final int code;

    /**
     * 构造函数，使用默认的BAD_REQUEST错误码
     * <p>
     * 默认错误码为400 (RestBean.BAD_REQUEST)
     * </p>
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = RestBean.BAD_REQUEST;
    }

    /**
     * 构造函数，自定义错误码
     * <p>
     * 允许指定自定义的错误码和错误消息
     * </p>
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数，包含原始异常
     * <p>
     * 用于包装原始异常，保留异常链
     * 默认错误码为400 (RestBean.BAD_REQUEST)
     * </p>
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = RestBean.BAD_REQUEST;
    }
}