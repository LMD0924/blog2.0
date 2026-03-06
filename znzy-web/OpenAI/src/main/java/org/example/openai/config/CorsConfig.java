package org.example.openai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域资源共享(CORS)配置类
 * <p>
 * 实现WebMvcConfigurer接口，用于配置跨域请求的规则
 * 允许前端应用从不同的域访问后端API
 * </p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    /**
     * 配置跨域请求映射规则
     * <p>
     * 该方法设置了所有API接口的跨域访问规则
     * </p>
     * @param registry CORS注册器，用于配置跨域规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 配置所有API接口都支持跨域
                .allowedOriginPatterns("*") // 允许所有来源访问，也可以配置具体的域名
                .allowCredentials(true) // 允许发送Cookie
                .allowedMethods("*") // 允许所有HTTP方法(GET, HEAD, POST, PUT, DELETE, OPTIONS等)
                .maxAge(3600); // 预检请求的缓存时间（秒），避免重复发送预检请求
    }
}
