package com.example.backend.controller.AI;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/*
 * @Author:总会落叶
 * @Date:2025/11/3
 * @Description:
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {
    private final ChatClient chatClient;

    @GetMapping(value = "/chat",produces = "text/plain;charset=utf-8")
    public Flux<String> chat(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
