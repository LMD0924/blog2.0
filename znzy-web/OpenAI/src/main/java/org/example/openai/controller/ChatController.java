package org.example.openai.controller;

import lombok.RequiredArgsConstructor;
import org.example.openai.model.ChatMessage;
import org.example.openai.model.ChatSession;
import org.example.openai.service.ChatMemoryService;
import org.example.openai.service.ChatSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI聊天控制器
 * 处理与AI模型的交互，包括流式聊天、历史记录管理、会话管理等功能
 */
@RequiredArgsConstructor  // Lombok注解，为final字段生成构造函数
@RestController  // 声明为REST控制器
@RequestMapping("/ai")  // 所有接口的基础路径为/ai
public class ChatController {
    // 聊天客户端，用于调用AI模型
    private final ChatClient chatClient;
    // 聊天记忆服务，负责消息的存储和读取
    private final ChatMemoryService chatMemoryService;
    // 会话服务，负责会话的创建、删除和管理
    private final ChatSessionService chatSessionService;

    /**
     * 流式聊天接口
     * 支持会话记忆功能，返回流式响应（SSE/Streaming）
     *
     * @param prompt 用户输入的问题或对话内容，默认值为"你是谁？"
     * @param sessionId 会话ID，用于区分不同的对话会话，如果为空则使用默认会话
     * @return Flux<String> 流式响应，逐块返回AI生成的内容
     */
    @GetMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public Flux<String> chat(
            @RequestParam(defaultValue = "你是谁？") String prompt,
            @RequestParam(required = false) String sessionId) {

        // 处理会话ID：如果未提供则使用默认值"default"
        String effectiveSessionId = (sessionId != null && !sessionId.isEmpty()) ? sessionId : "default";

        // 加载最近的历史消息，用于构建上下文
        List<ChatMessage> history = chatMemoryService.loadRecentHistory(effectiveSessionId);

        // 将历史消息和当前问题组合成完整的提示词
        String contextPrompt = buildPromptWithHistory(history, prompt);

        // 先保存用户的当前消息到数据库
        // 这样做确保了即使AI响应出错，用户的输入也会被记录
        chatMemoryService.saveMessage(effectiveSessionId, "user", prompt);

        // 用于收集AI的完整响应
        StringBuilder aiResponse = new StringBuilder();

        // 构建聊天请求并获取流式响应
        return chatClient.prompt()
                .user(contextPrompt)  // 设置用户输入的提示词
                .stream()             // 开启流式模式
                .content()            // 提取内容部分
                .doOnNext(chunk -> aiResponse.append(chunk))  // 每收到一个数据块就追加到响应中
                .doOnComplete(() -> {
                    // 当流式响应完成时，保存AI的完整回答到数据库
                    if (aiResponse.length() > 0) {
                        chatMemoryService.saveMessage(effectiveSessionId, "assistant", aiResponse.toString());
                    }
                });
    }

    /**
     * 获取指定会话的历史消息记录
     *
     * @param sessionId 会话ID，用于标识要查询哪个会话的历史
     * @return List<ChatMessageResponse> 历史消息列表，包含角色、内容和时间戳
     */
    @GetMapping("/history")
    public List<ChatMessageResponse> history(@RequestParam String sessionId) {
        // 从数据库加载指定会话的全部历史消息
        List<ChatMessage> messages = chatMemoryService.loadFullHistory(sessionId);

        // 将数据库实体转换为API响应对象
        return messages.stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getRole(),        // 消息发送者角色：user/assistant/system
                        msg.getContent(),     // 消息内容
                        msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : ""  // 时间戳，确保不为null
                ))
                .toList();
    }

    /**
     * 清空指定会话的历史消息
     * 注意：只删除消息，不删除会话本身
     *
     * @param sessionId 要清空消息的会话ID
     */
    @DeleteMapping("/history")
    public void clearHistory(@RequestParam String sessionId) {
        chatMemoryService.deleteHistory(sessionId);
    }

    /**
     * 获取所有聊天会话列表
     * 通常用于展示用户的对话历史记录
     *
     * @return List<ChatSessionResponse> 会话列表，包含会话ID、标题、创建和更新时间
     */
    @GetMapping("/sessions")
    public List<ChatSessionResponse> sessions() {
        return chatSessionService.listSessions()
                .stream()
                .map(this::toSessionResponse)  // 转换每个会话实体为响应对象
                .toList();
    }

    /**
     * 创建新的聊天会话
     * 可以指定会话标题，如果未指定则可能由系统自动生成
     *
     * @param request 创建请求，包含可选的会话标题
     * @return ChatSessionResponse 新创建的会话信息
     */
    @PostMapping("/session")
    public ChatSessionResponse createSession(@RequestBody(required = false) CreateSessionRequest request) {
        // 提取请求中的标题，如果请求为空则标题为null
        String title = request != null ? request.title() : null;

        // 调用服务层创建会话
        ChatSession session = chatSessionService.createSession(title);

        // 返回响应对象
        return toSessionResponse(session);
    }

    /**
     * 删除指定会话
     * 注意：会同时删除该会话的所有历史消息和会话记录
     *
     * @param sessionId 要删除的会话ID
     */
    @DeleteMapping("/session")
    public void deleteSession(@RequestParam String sessionId) {
        // 先删除该会话的所有消息记录
        chatMemoryService.deleteHistory(sessionId);

        // 再删除会话记录本身
        chatSessionService.deleteSession(sessionId);
    }

    /**
     * 构建包含历史上下文的提示词
     * 将历史对话和当前问题组合，帮助AI理解对话上下文
     *
     * @param history 历史消息列表，按时间顺序排列
     * @param latestQuestion 当前用户的最新问题
     * @return 组合后的完整提示词
     */
    private String buildPromptWithHistory(List<ChatMessage> history, String latestQuestion) {
        StringBuilder builder = new StringBuilder();

        // 如果有历史记录，先添加历史上下文
        if (!history.isEmpty()) {
            builder.append("以下是之前的对话历史：\n");

            // 遍历历史消息，按角色和内容格式展示
            for (ChatMessage message : history) {
                builder.append(message.getRole())   // 角色：user/assistant
                        .append(": ")
                        .append(message.getContent())  // 消息内容
                        .append("\n");
            }
            builder.append("\n现在请回答以下问题：\n");
        }

        // 添加当前问题
        builder.append(latestQuestion);

        return builder.toString();
    }

    /**
     * 聊天消息响应对象（DTO）
     * 用于API响应，不暴露数据库实体细节
     *
     * @param role 消息角色：user（用户）、assistant（AI助手）、system（系统）
     * @param content 消息内容
     * @param timestamp 消息创建时间
     */
    public record ChatMessageResponse(String role, String content, String timestamp) {}

    /**
     * 聊天会话响应对象（DTO）
     * 用于API响应，不暴露数据库实体细节
     *
     * @param sessionId 会话唯一标识符
     * @param title 会话标题
     * @param createdAt 会话创建时间
     * @param updatedAt 会话最后更新时间
     */
    public record ChatSessionResponse(String sessionId, String title, String createdAt, String updatedAt) {}

    /**
     * 创建会话请求对象（DTO）
     * 用于接收前端创建会话的请求
     *
     * @param title 可选的会话标题
     */
    public record CreateSessionRequest(String title) {}

    /**
     * 将数据库会话实体转换为API响应对象
     *
     * @param session 数据库会话实体
     * @return 转换后的响应对象
     */
    private ChatSessionResponse toSessionResponse(ChatSession session) {
        return new ChatSessionResponse(
                session.getSessionId(),  // 会话ID
                session.getTitle(),      // 会话标题
                session.getCreatedAt() != null ? session.getCreatedAt().toString() : "",  // 创建时间
                session.getUpdatedAt() != null ? session.getUpdatedAt().toString() : ""   // 更新时间
        );
    }
}