package io.throttle.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Throttle 라이브러리를 위한 웹 설정
 * CORS 설정 등을 포함
 */
@Configuration
@ConditionalOnProperty(name = "api-throttle.enabled", havingValue = "true", matchIfMissing = true)
public class ThrottleWebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")  // 모든 origin 허용 (개발용)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders(
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining", 
                "X-RateLimit-Reset",
                "X-Quota-Daily-Limit",
                "X-Quota-Daily-Remaining",
                "X-Quota-Daily-Reset",
                "X-Quota-Monthly-Limit",
                "X-Quota-Monthly-Remaining",
                "X-Quota-Monthly-Reset",
                "Retry-After"
            )
            .allowCredentials(true)
            .maxAge(3600); // 1시간
    }
} 