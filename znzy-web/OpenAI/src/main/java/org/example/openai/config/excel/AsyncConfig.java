package org.example.openai.config.excel;

/*
 * @Author:总会落叶
 * @Date:2025/12/25
 * @Description:
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 线程池配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("excelAsyncExecutor")
    public Executor excelAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("excel-async-");
        executor.initialize();
        return executor;
    }
}