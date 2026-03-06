package org.example.openai.config.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/6
 * @Description:
 */

import org.example.openai.model.RestBean;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类
 * <p>
 * 使用@RestControllerAdvice注解，用于统一处理所有Controller层抛出的异常
 * 将异常信息转换为统一的RESTful响应格式
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理文件大小超出限制异常
     * <p>
     * 当上传的文件大小超过配置的最大值时抛出此异常
     * </p>
     * @param e MaxUploadSizeExceededException异常对象
     * @return RestBean 包含错误信息的统一响应格式
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RestBean<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("maxSize", "10MB");
        errorInfo.put("errorType", "FILE_TOO_LARGE");

        return RestBean.failure(RestBean.BAD_REQUEST, "文件大小不能超过10MB");
    }

    /**
     * 处理参数验证异常
     * <p>
     * 当@Valid或@Validated注解的请求参数验证失败时抛出此异常
     * </p>
     * @param e MethodArgumentNotValidException异常对象，包含验证失败的详细信息
     * @return RestBean 包含字段错误信息的统一响应格式
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestBean<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        // 遍历所有验证错误，将字段名和错误信息存入Map
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return RestBean.failure(RestBean.BAD_REQUEST, "参数验证失败");
    }

    /**
     * 处理参数绑定异常
     * <p>
     * 当请求参数与方法参数绑定失败时抛出此异常
     * </p>
     * @param e BindException异常对象，包含绑定失败的详细信息
     * @return RestBean 包含字段错误信息的统一响应格式
     */
    @ExceptionHandler(BindException.class)
    public RestBean<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        // 遍历所有绑定错误，将字段名和错误信息存入Map
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return RestBean.failure(RestBean.BAD_REQUEST, "参数绑定失败");
    }

    /**
     * 处理业务异常
     * <p>
     * 当业务逻辑验证失败时抛出的自定义异常
     * </p>
     * @param e BusinessException异常对象，包含业务错误码和错误信息
     * @return RestBean 包含业务错误信息的统一响应格式
     */
    @ExceptionHandler(BusinessException.class)
    public RestBean<String> handleBusinessException(BusinessException e) {
        // 使用异常中定义的错误码和错误信息返回响应
        return RestBean.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理所有其他未捕获的异常
     * <p>
     * 作为全局异常处理的兜底方法，处理所有未被专门处理的异常
     * </p>
     * @param e Exception异常对象，包含异常信息
     * @return RestBean 包含通用错误信息的统一响应格式
     */
    @ExceptionHandler(Exception.class)
    public RestBean<String> handleGlobalException(Exception e) {
        // 返回通用的系统错误信息
        return RestBean.failure(RestBean.INTERNAL_ERROR, "系统繁忙，请稍后重试");
    }
}