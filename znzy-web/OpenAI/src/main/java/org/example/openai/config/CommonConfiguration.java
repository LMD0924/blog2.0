package org.example.openai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @Author:总会落叶
 * @Date:2025/11/3
 * @Description: 实现会话记忆的配置
 * 注意：由于Spring AI API的限制，会话记忆功能在Controller中通过sessionId参数实现
 * 前端会传递sessionId，后端可以根据sessionId维护对话历史
 */

/**
 * 通用配置类
 * <p>
 * 用于配置Spring AI相关的组件，包括ChatClient的创建和配置
 * 该类负责设置与AI模型交互的基础参数
 * </p>
 */
@Configuration
public class CommonConfiguration {

    /**
     * 创建并配置ChatClient实例
     * <p>
     * 该方法使用OpenAiChatModel创建一个配置好的ChatClient实例，用于与OpenAI模型进行交互
     * 配置包括默认系统提示和日志顾问
     * </p>
     * @param model OpenAI聊天模型实例，由Spring自动注入
     * @return 配置好的ChatClient实例
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient
                .builder(model) // 创建ChatClient工厂
                .defaultSystem("你是总会落叶的专属助手，你叫总会落叶，有什么可以帮助你的。") // 设置默认系统提示
                .defaultAdvisors(
                        new SimpleLoggerAdvisor() // 添加日志顾问，用于记录对话内容
                )
                .build(); // 构建ChatClient实例
    }
}
