package io.throttle.spring.config;

import io.throttle.spring.starter.ThrottleManager;
import io.throttle.core.api.QuotaService;
import io.throttle.core.api.RateLimitService;
import io.throttle.memory.impl.InMemoryQuotaService;
import io.throttle.memory.impl.InMemoryRateLimitService;
import io.throttle.redis.impl.RedisQuotaService;
import io.throttle.redis.impl.RedisRateLimitService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Spring Boot Auto Configuration for Throttle Library
 */
@Configuration
@ConditionalOnClass(ThrottleManager.class)
@EnableConfigurationProperties(ThrottleProperties.class)
@ConditionalOnProperty(prefix = "api-throttle", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThrottleAutoConfiguration {
    
    private final ThrottleProperties properties;
    
    public ThrottleAutoConfiguration(ThrottleProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "memory", matchIfMissing = true)
    public QuotaService inMemoryQuotaService() {
        return new InMemoryQuotaService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "redis")
    @ConditionalOnClass(RedisTemplate.class)
    public QuotaService redisQuotaService(RedisTemplate<String, String> redisTemplate) {
        return new RedisQuotaService(redisTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "memory", matchIfMissing = true)
    public RateLimitService inMemoryRateLimitService() {
        return new InMemoryRateLimitService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "redis")
    @ConditionalOnClass(RedisTemplate.class)
    public RateLimitService redisRateLimitService(RedisTemplate<String, String> redisTemplate) {
        return new RedisRateLimitService(redisTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ThrottleManager throttleManager(QuotaService quotaService, RateLimitService rateLimitService) {
        return new ThrottleManager(rateLimitService, quotaService);
    }
} 