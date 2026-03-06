package org.example.openai.service;

import lombok.RequiredArgsConstructor;
import org.example.openai.model.ChatSession;
import org.example.openai.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话服务类
 * 负责管理聊天会话的创建、维护、更新和删除等业务逻辑
 * 处理会话的生命周期管理和标题生成等智能功能
 */
@Service  // Spring注解，标记为服务层组件
@RequiredArgsConstructor  // Lombok注解，为final字段生成构造函数
public class ChatSessionService {

    // 会话数据访问层，负责数据库操作
    private final ChatSessionRepository chatSessionRepository;

    /**
     * 创建新地聊天会话
     * 生成唯一的会话ID并保存到数据库
     *
     * @param title 会话标题（可为null，系统会根据后续消息自动生成）
     * @return 新创建的会话对象
     */
    public ChatSession createSession(String title) {
        // 创建新的会话对象
        ChatSession session = new ChatSession();
        session.setSessionId(generateSessionId());      // 生成唯一ID
        session.setTitle(title);                        // 设置标题（可能为null）
        session.setCreatedAt(LocalDateTime.now());      // 设置创建时间
        session.setUpdatedAt(session.getCreatedAt());   // 初始时更新时间和创建时间相同

        // 保存到数据库并返回
        return chatSessionRepository.save(session);
    }

    /**
     * 确保会话存在（如果不存在则创建）
     * 这是一个"存在即返回，不存在则创建"的原子操作
     * 常用于处理默认会话或不确定会话是否存在的场景
     *
     * @param sessionId 要确保存在的会话ID
     * @param initialTitle 如果创建新会话时使用的初始标题（可为null）
     * @return 存在或新创建的会话对象
     */
    public ChatSession ensureSessionExists(String sessionId, String initialTitle) {
        // 1. 先尝试查找现有会话
        ChatSession existing = chatSessionRepository.findBySessionId(sessionId);
        if (existing != null) {
            return existing;  // 如果已存在，直接返回
        }

        // 2. 如果不存在，创建新会话
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);               // 使用传入的sessionId
        session.setTitle(initialTitle);                // 设置初始标题
        session.setCreatedAt(LocalDateTime.now());     // 设置创建时间
        session.setUpdatedAt(session.getCreatedAt());  // 初始更新时间

        // 保存并返回
        return chatSessionRepository.save(session);
    }

    /**
     * 记录用户消息，更新会话状态
     * 这个方法是智能标题生成的核心逻辑：
     * 1. 确保会话存在
     * 2. 如果会话标题为空，使用用户消息生成标题
     * 3. 更新会话的活跃时间
     *
     * @param sessionId 会话ID
     * @param userMessage 用户消息内容（用于生成标题）
     */
    public void recordUserMessage(String sessionId, String userMessage) {
        // 1. 确保会话存在，使用用户消息的前30字符作为可能的初始标题
        ensureSessionExists(sessionId, trimTitle(userMessage));

        // 2. 如果当前标题为空，使用用户消息生成标题
        // 注意：只会在标题为空时更新，避免覆盖用户手动设置的标题
        chatSessionRepository.updateTitleIfEmpty(sessionId, trimTitle(userMessage));

        // 3. 更新会话的活跃时间（最后更新时间）
        chatSessionRepository.touchSession(sessionId, LocalDateTime.now());
    }

    /**
     * 记录AI助手消息，更新会话状态
     * 比用户消息简单，只更新活跃时间
     *
     * @param sessionId 会话ID
     */
    public void recordAssistantMessage(String sessionId) {
        // 1. 确保会话存在（可能传入null标题）
        ensureSessionExists(sessionId, null);

        // 2. 更新会话的活跃时间
        chatSessionRepository.touchSession(sessionId, LocalDateTime.now());
    }

    /**
     * 获取所有会话列表
     * 按更新时间倒序排列，最新的会话在前
     *
     * @return 所有会话的列表
     */
    public List<ChatSession> listSessions() {
        return chatSessionRepository.findAll();
    }

    /**
     * 删除指定会话
     * 注意：需要先删除关联的消息记录（由上层逻辑处理）
     *
     * @param sessionId 要删除的会话ID
     */
    public void deleteSession(String sessionId) {
        chatSessionRepository.deleteBySessionId(sessionId);
    }

    /**
     * 获取指定会话的详细信息
     *
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回null
     */
    public ChatSession getSession(String sessionId) {
        return chatSessionRepository.findBySessionId(sessionId);
    }

    /**
     * 生成唯一的会话ID
     * 使用UUID保证全局唯一性
     *
     * @return 唯一的会话ID字符串
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 智能标题修剪函数
     * 将用户消息转换为适合作为标题的格式：
     * 1. 去除前后空格
     * 2. 如果超过30字符，截断并添加"..."
     * 3. 用于自动生成会话标题
     *
     * @param content 原始内容（用户消息）
     * @return 修剪后的标题字符串，如果输入为null则返回null
     */
    private String trimTitle(String content) {
        if (content == null) {
            return null;
        }

        // 1. 去除前后空格
        String trimmed = content.strip();

        // 2. 如果长度不超过30字符，直接返回
        if (trimmed.length() <= 30) {
            return trimmed;
        }

        // 3. 超过30字符时截断并添加省略号
        return trimmed.substring(0, 30) + "...";
    }
}