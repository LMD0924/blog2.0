package org.example.openai.model;

/*
 * @Author:总会落叶
 * @Date:2025/12/6
 * @Description:
 */
// RestBean.java

import lombok.Data;

/**
 * REST API 统一响应封装类
 * <p>
 * 用于统一API的响应格式，包含状态码、消息、数据和时间戳
 * 支持泛型数据类型，可灵活适应不同的响应数据结构
 * </p>
 * @param <T> 响应数据的类型
 */
@Data
public class RestBean<T> {
    /**
     * 状态码
     * 表示请求的处理结果，参考HTTP状态码规范
     */
    private int code;      
    
    /**
     * 响应消息
     * 对请求结果的文字描述，便于前端显示或调试
     */
    private String message; 
    
    /**
     * 响应数据
     * 请求成功时返回的具体数据，类型由泛型参数指定
     */
    private T data;        
    
    /**
     * 时间戳
     * 响应生成的时间，精确到毫秒
     */
    private long timestamp; 

    /**
     * 私有构造方法
     * <p>
     * 防止外部直接创建实例，确保使用静态工厂方法创建响应对象
     * </p>
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    private RestBean(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建成功响应（无数据）
     * <p>
     * 适用于只需要提示成功但不需要返回具体数据的场景
     * </p>
     * @param <T> 响应数据类型
     * @return 成功响应对象
     */
    public static <T> RestBean<T> success() {
        return new RestBean<>(200, "操作成功", null);
    }

    /**
     * 创建成功响应（带数据）
     * <p>
     * 适用于需要返回具体数据的成功场景
     * </p>
     * @param <T> 响应数据类型
     * @param data 响应数据
     * @return 带数据的成功响应对象
     */
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200, "操作成功", data);
    }

    /**
     * 创建成功响应（自定义消息）
     * <p>
     * 适用于需要自定义成功消息和返回数据的场景
     * </p>
     * @param <T> 响应数据类型
     * @param message 自定义成功消息
     * @param data 响应数据
     * @return 自定义消息的成功响应对象
     */
    public static <T> RestBean<T> success(String message, T data) {
        return new RestBean<>(200, message, data);
    }

    /**
     * 创建失败响应
     * <p>
     * 适用于需要自定义错误码和错误消息的场景
     * </p>
     * @param <T> 响应数据类型
     * @param code 错误状态码
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(code, message, null);
    }

    /**
     * 创建失败响应（默认错误码）
     * <p>
     * 适用于使用默认错误码（400）的失败场景
     * </p>
     * @param <T> 响应数据类型
     * @param message 错误消息
     * @return 默认错误码的失败响应对象
     */
    public static <T> RestBean<T> failure(String message) {
        return new RestBean<>(400, message, null);
    }

    // 常用状态码常量
    public static final int SUCCESS = 200;          // 成功
    public static final int BAD_REQUEST = 400;      // 请求参数错误
    public static final int UNAUTHORIZED = 401;     // 未授权
    public static final int FORBIDDEN = 403;        // 禁止访问
    public static final int NOT_FOUND = 404;        // 资源不存在
    public static final int INTERNAL_ERROR = 500;   // 服务器内部错误
}
