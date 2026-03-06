package org.example.openai.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 聊天会话模型
 * <p>
 * 用于管理用户与AI之间的会话信息
 * 每个会话包含多个聊天消息
 * </p>
 */
@Getter
@Setter
public class ChatSession {
    /**
     * 会话ID
     * 主键，自增
     */
    private Long id;
    
    /**
     * 会话标识
     * 用于前端与后端通信的会话唯一标识
     */
    private String sessionId;
    
    /**
     * 会话标题
     * 显示在会话列表中的名称，通常由系统根据会话内容自动生成
     */
    private String title;
    
    /**
     * 创建时间
     * 会话创建的时间戳
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     * 会话最后一次活动的时间戳
     */
    private LocalDateTime updatedAt;
}


