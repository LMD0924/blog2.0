package org.example.openai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenAI应用程序主类
 * <p>
 * 这是Spring Boot应用程序的入口点，使用@SpringBootApplication注解自动配置应用程序上下文
 * 该类负责启动整个OpenAI相关的后端服务
 * </p>
 */
@SpringBootApplication
public class OpenAiApplication {

    /**
     * 应用程序入口方法
     * <p>
     * 该方法使用SpringApplication.run()启动应用程序，加载所有Spring组件并初始化应用上下文
     * </p>
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OpenAiApplication.class, args);
    }

}
