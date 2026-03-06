package org.example.openai.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 聊天消息模型
 * <p>
 * 用于在业务层与数据库之间传递聊天消息数据
 * 记录用户与AI之间的对话内容
 * </p>
 */
@Getter
@Setter
public class ChatMessage {
    /**
     * 消息ID
     * 主键，自增
     */
    private Long id;
    
    /**
     * 会话ID
     * 关联到聊天会话，用于分组消息
     */
    private String sessionId;
    
    /**
     * 角色
     * 可能的值：user（用户）、assistant（AI助手）、system（系统）
     */
    private String role;
    
    /**
     * 消息内容
     * 聊天的具体文本内容
     */
    private String content;
    
    /**
     * 创建时间
     * 消息发送的时间戳
     */
    private LocalDateTime createdAt;
}

