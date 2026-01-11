package com.example.backend.config.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @Author:总会落叶
 * @Date:2025/11/3
 * @Description:
 */
@Configuration
public class CommonConfiguration {
    @Bean
    public ChatClient chatClient(OpenAiChatModel model){
        return ChatClient
                .builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
