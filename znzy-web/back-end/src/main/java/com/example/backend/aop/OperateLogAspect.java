package com.example.backend.aop;

import com.example.backend.annot.Log;
import com.example.backend.entity.OperateLog;
import com.example.backend.mapper.OperateLogMapper;
import com.example.backend.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/*
 * @Author:总会落叶
 * @Date:2025/12/23
 * @Description:
 */
@Aspect
@Component
public class OperateLogAspect {
    @Autowired
    private OperateLogMapper operateLogMapper;
    @Autowired
    private HttpServletRequest request;
    /**
     * 环绕通知记录操作日志
     * @param pjp 连接点
     * @param logAnnotation @Log注解
     * @return 原始方法的返回值
     */
    @Around("@annotation(logAnnotation)")
    public Object recordLog(ProceedingJoinPoint pjp, Log logAnnotation) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 创建日志对象
        OperateLog operateLog = new OperateLog();
        operateLog.setOperateTime(LocalDateTime.now());
        operateLog.setSuccess(true);

        Object result = null;

        try {
            // 1. 获取方法信息
            String methodName = pjp.getSignature().getName();
            operateLog.setMethodName(methodName);
            System.out.println(methodName);
            // 2. 获取类信息
            String className = pjp.getTarget().getClass().getName();
            operateLog.setClassName(className);
            System.out.println(className);
            // 3. 获取当前用户信息
            Integer userId = UserContext.getCurrentUserId();
            String username = UserContext.getCurrentUserName();
            String avatar=UserContext.getCurrentUserAvatar();
            if (userId != null) {
                operateLog.setUserId(userId);
                operateLog.setUserName(username);
                operateLog.setAvatar(avatar);
                System.out.println("当前用户ID：" + userId);
                System.out.println("当前用户名：" + username);
                System.out.println("当前用户头像：" + avatar);
            } else {
                System.out.println("当前用户未登录");
            }

            // 4. 获取请求信息
            if (request != null) {
                operateLog.setRequestUri(request.getRequestURI());
                operateLog.setClientIp(getClientIp(request));
                System.out.println("请求URI：" + request.getRequestURI());
            }

            // 5. 执行原始方法
            result = pjp.proceed();

            // 6. 计算耗时
            long endTime = System.currentTimeMillis();
            operateLog.setCostTime(endTime - startTime);

            // 7. 异步保存日志
            //saveLogAsync(operateLog);

            return result;

        } catch (Throwable throwable) {
            // 8. 异常处理
            long endTime = System.currentTimeMillis();
            operateLog.setCostTime(endTime - startTime);
            operateLog.setSuccess(false);
            operateLog.setErrorMessage(throwable.getMessage());

            // 异步保存错误日志
            //saveLogAsync(operateLog);

            // 重新抛出异常，让调用者知道
            throw throwable;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 异步保存操作日志（不影响主流程性能）
     */
/*    private void saveLogAsync(OperateLog operateLog) {
        CompletableFuture.runAsync(() -> {
            try {
                int rows = operateLogMapper.addOperateLog(operateLog);
                if (rows > 0) {
                    System.out.println("操作日志保存成功");
                }
            } catch (Exception e) {
                System.out.println("操作日志保存失败：" + e.getMessage());
            }
        });
    }*/
}