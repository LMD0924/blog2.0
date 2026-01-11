package com.example.backend.config;

import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/*
 * @Author:总会落叶
 * @Date:2025/11/16
 * @Description: WebSocket配置类，用于配置WebSocket端点和处理器
 */
public class WebSocketConfig implements WebSocketConfigurer {
    /**
     * 注册WebSocket处理器的方法
     * @param registry WebSocket处理器注册器，用于注册WebSocket处理器和对应的端点
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册通知WebSocket处理器，并将其映射到"/ws/notifications"端点
        // 同时允许所有来源的跨域请求
        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notifications")
                .setAllowedOrigins("*");
    }
}
