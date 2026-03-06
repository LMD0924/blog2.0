package org.example.openai.service;

import org.example.openai.model.ChatMessage;
import org.example.openai.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 聊天记忆服务类
 * 负责管理聊天消息的记忆逻辑，包括消息的保存、加载、清理等操作
 * 作为Controller层和Repository层之间的桥梁，封装业务逻辑
 */
@Service  // Spring注解，标记为服务层组件，处理业务逻辑
public class ChatMemoryService {

    /**
     * 历史消息最大保留数量常量
     * 用于控制单个会话的最大消息数量，避免：
     * 1. 数据库无限增长
     * 2. AI上下文token超限
     * 3. 内存占用过高
     */
    private static final int MAX_HISTORY_MESSAGES = 40;

    // 消息数据访问层，负责数据库操作
    private final ChatMessageRepository chatMessageRepository;

    // 会话服务，用于更新会话相关信息
    private final ChatSessionService chatSessionService;

    /**
     * 构造函数，通过Spring依赖注入
     *
     * @param chatMessageRepository 消息仓库，用于数据库操作
     * @param chatSessionService 会话服务，用于更新会话状态
     */
    public ChatMemoryService(ChatMessageRepository chatMessageRepository,
                             ChatSessionService chatSessionService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionService = chatSessionService;
    }

    /**
     * 加载指定会话的最近历史消息
     * 用于构建AI对话的上下文，只返回最新的消息
     *
     * @param sessionId 会话ID
     * @return 按时间正序排列的最近历史消息列表
     *         如果sessionId为空，返回空列表
     */
    public List<ChatMessage> loadRecentHistory(String sessionId) {
        // 参数校验：确保sessionId有效
        if (sessionId == null || sessionId.isEmpty()) {
            return Collections.emptyList();  // 返回不可变的空列表，避免NullPointerException
        }

        // 从数据库加载最近的历史消息，数量限制为MAX_HISTORY_MESSAGES
        return chatMessageRepository.findRecentHistory(sessionId, MAX_HISTORY_MESSAGES);
    }

    /**
     * 保存一条消息到数据库
     * 这是核心方法，负责：
     * 1. 创建消息对象
     * 2. 根据消息角色更新会话状态
     * 3. 保存到数据库
     * 4. 清理超出的历史消息
     *
     * @param sessionId 会话ID
     * @param role 消息角色：user（用户）或 assistant（AI助手）
     * @param content 消息内容
     * @return 保存后的消息对象（包含数据库生成的ID）
     */
    public ChatMessage saveMessage(String sessionId, String role, String content) {
        // 1. 创建消息对象并设置属性
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);           // 设置会话ID
        message.setRole(role);                     // 设置消息角色
        message.setContent(content);               // 设置消息内容
        message.setCreatedAt(LocalDateTime.now()); // 设置当前时间为创建时间

        // 2. 根据消息角色更新会话状态
        if ("user".equals(role)) {
            // 如果是用户消息，记录到会话服务（可能用于自动生成标题）
            chatSessionService.recordUserMessage(sessionId, content);
        } else {
            // 如果是AI助手消息，更新会话的最后活跃时间
            chatSessionService.recordAssistantMessage(sessionId);
        }

        // 3. 保存消息到数据库
        ChatMessage saved = chatMessageRepository.save(message);

        // 4. 清理超出的历史消息，保持总数不超过MAX_HISTORY_MESSAGES
        chatMessageRepository.trimHistory(sessionId, MAX_HISTORY_MESSAGES);

        return saved;
    }

    /**
     * 删除指定会话的所有历史消息
     * 用于用户主动清空聊天记录的场景
     *
     * @param sessionId 要清空消息的会话ID
     */
    public void deleteHistory(String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
    }

    /**
     * 加载指定会话的完整历史消息
     * 用于在界面展示完整的对话历史，不受数量限制
     *
     * @param sessionId 会话ID
     * @return 按时间正序排列的完整历史消息列表
     *         如果sessionId为空，返回空列表
     */
    public List<ChatMessage> loadFullHistory(String sessionId) {
        // 参数校验
        if (sessionId == null || sessionId.isEmpty()) {
            return Collections.emptyList();
        }

        // 从数据库加载指定会话的所有消息
        return chatMessageRepository.findFullHistory(sessionId);
    }
}