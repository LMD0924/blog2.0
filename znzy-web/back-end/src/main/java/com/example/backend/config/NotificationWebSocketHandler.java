package com.example.backend.config;

/*
 * @Author:总会落叶
 * @Date:2025/11/9
 * @Description: WebSocket通知处理器 - 用于实现服务器向客户端的实时消息推送
 * 功能：处理WebSocket连接、消息收发、会话管理、实时通知推送等
 */
import com.example.backend.entity.Notice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket通知处理器类
 * 继承TextWebSocketHandler，专门处理文本格式的WebSocket消息
 * 采用单例模式管理所有WebSocket会话
 */
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储所有活跃的WebSocket会话
     * 使用CopyOnWriteArraySet保证线程安全：
     * - 读操作无锁，性能高
     * - 写操作时复制新集合，适合读多写少的场景
     */
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    /**
     * JSON序列化/反序列化工具
     * 用于在Java对象和JSON字符串之间转换
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * WebSocket连接建立成功后的回调方法
     * @param session 新建立的WebSocket会话对象
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 将新会话添加到会话集合中
        sessions.add(session);
        System.out.println("WebSocket连接建立: " + session.getId());

        // 向客户端发送连接成功确认消息
        sendMessage(session, new WebSocketMessage("connected", "连接成功"));
    }

    /**
     * 处理客户端发送的文本消息
     * @param session 发送消息的会话对象
     * @param message 客户端发送的消息内容
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 获取消息的实际内容
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);

        // 处理客户端的心跳检测机制
        // 当客户端发送"ping"时，服务器回复"pong"以保持连接活跃
        if ("ping".equals(payload)) {
            sendMessage(session, new WebSocketMessage("pong", ""));
        }
        // 可以在此处扩展处理其他类型的客户端指令
    }

    /**
     * WebSocket连接关闭后的回调方法
     * @param session 关闭的会话对象
     * @param status 连接关闭的状态信息
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从会话集合中移除已关闭的会话
        sessions.remove(session);
        System.out.println("WebSocket连接关闭: " + session.getId() + ", 关闭原因: " + status.getReason());
    }

    /**
     * WebSocket传输过程中发生错误时的回调方法
     * @param session 发生错误的会话对象
     * @param exception 异常信息
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
        // 发生错误时从会话集合中移除该会话
        sessions.remove(session);
    }

    // ========== 公共消息发送方法 - 供业务层调用 ==========

    /**
     * 广播通知给所有连接的客户端
     * 使用场景：系统公告、全局通知等需要所有用户接收的消息
     * @param notice 要广播的通知对象
     */
    public static void broadcastNotice(Notice notice) {
        // 创建WebSocket消息对象，类型为"new_notice"
        WebSocketMessage message = new WebSocketMessage("new_notice", notice);
        // 调用广播方法发送消息
        broadcastMessage(message);
    }

    /**
     * 发送通知给特定用户
     * 注意：当前实现为简化版本，实际需要根据用户ID进行会话过滤
     * @param userId 目标用户ID
     * @param notice 要发送的通知对象
     */
    public static void sendNoticeToUser(Integer userId, Notice notice) {
        WebSocketMessage message = new WebSocketMessage("new_notice", notice);
        // 遍历所有会话（实际应该根据userId过滤特定用户的会话）
        sessions.forEach(session -> {
            try {
                // TODO: 需要完善用户会话识别逻辑
                // 当前简单实现：广播给所有用户
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                }
            } catch (IOException e) {
                System.err.println("发送消息失败: " + e.getMessage());
            }
        });
    }

    // ========== 私有工具方法 ==========

    /**
     * 广播消息给所有连接的客户端
     * @param message 要广播的WebSocket消息对象
     */
    private static void broadcastMessage(WebSocketMessage message) {
        // 遍历所有活跃会话
        sessions.forEach(session -> {
            try {
                // 检查会话是否仍然打开
                if (session.isOpen()) {
                    // 将消息对象转换为JSON字符串并发送
                    session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(message)));
                }
            } catch (IOException e) {
                System.err.println("广播消息失败: " + e.getMessage());
            }
        });
    }

    /**
     * 向单个会话发送消息
     * @param session 目标会话对象
     * @param message 要发送的消息对象
     * @throws IOException 消息发送失败时抛出IO异常
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        // 确保会话仍然活跃
        if (session.isOpen()) {
            // 序列化消息对象为JSON并发送
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }
    }

    // ========== 内部消息包装类 ==========

    /**
     * WebSocket消息包装类
     * 统一WebSocket消息格式，包含类型、数据和时间戳
     */
    public static class WebSocketMessage {
        /**
         * 消息类型：
         * - "connected": 连接成功
         * - "new_notice": 新通知
         * - "pong": 心跳响应
         */
        private String type;

        /**
         * 消息数据：可以是字符串、对象等任意类型
         * 对于通知类型，通常是Notice对象
         */
        private Object data;

        /**
         * 消息时间戳：服务器生成消息的时间
         * 用于客户端消息排序和去重
         */
        private Long timestamp;

        /**
         * 构造函数
         * @param type 消息类型
         * @param data 消息数据
         */
        public WebSocketMessage(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis(); // 使用系统当前时间戳
        }

        // ========== Getter和Setter方法 ==========

        /**
         * 获取消息类型
         * @return 消息类型字符串
         */
        public String getType() { return type; }

        /**
         * 设置消息类型
         * @param type 消息类型
         */
        public void setType(String type) { this.type = type; }

        /**
         * 获取消息数据
         * @return 消息数据对象
         */
        public Object getData() { return data; }

        /**
         * 设置消息数据
         * @param data 消息数据
         */
        public void setData(Object data) { this.data = data; }

        /**
         * 获取消息时间戳
         * @return 时间戳（毫秒）
         */
        public Long getTimestamp() { return timestamp; }

        /**
         * 设置消息时间戳
         * @param timestamp 时间戳
         */
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}