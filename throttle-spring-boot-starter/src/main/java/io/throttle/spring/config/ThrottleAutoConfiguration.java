package io.throttle.spring.config;

import io.throttle.spring.starter.ThrottleManager;
import io.throttle.spring.starter.RequestKeyGenerator;
import io.throttle.core.api.QuotaService;
import io.throttle.core.api.RateLimitService;
import io.throttle.memory.impl.InMemoryQuotaService;
import io.throttle.memory.impl.InMemoryRateLimitService;
import io.throttle.redis.impl.RedisQuotaService;
import io.throttle.redis.impl.RedisRateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import io.throttle.spring.starter.aspect.RateLimitAspect;
import io.throttle.spring.starter.aspect.ApiQuotaAspect;
import io.throttle.spring.starter.ThrottleResponseAdvice;
import io.throttle.spring.starter.ThrottleExceptionHandler;

/**
 * Spring Boot Auto Configuration for Throttle Library
 */
@Configuration
@EnableConfigurationProperties(ThrottleProperties.class)
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "api-throttle", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThrottleAutoConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(ThrottleAutoConfiguration.class);
    
    private final ThrottleProperties properties;
    
    public ThrottleAutoConfiguration(ThrottleProperties properties) {
        this.properties = properties;
        log.info("=== ThrottleAutoConfiguration initialized ===");
        log.info("Throttle properties: enabled={}, defaultStorage={}", 
                properties.isEnabled(), properties.getDefaultStorage());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RequestKeyGenerator requestKeyGenerator() {
        log.info("Creating RequestKeyGenerator bean");
        return new RequestKeyGenerator(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "memory", matchIfMissing = true)
    public QuotaService inMemoryQuotaService() {
        log.info("Creating InMemoryQuotaService bean");
        return new InMemoryQuotaService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "redis")
    @ConditionalOnClass(RedisTemplate.class)
    public QuotaService redisQuotaService(RedisTemplate<String, String> redisTemplate) {
        log.info("Creating RedisQuotaService bean");
        return new RedisQuotaService(redisTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "memory", matchIfMissing = true)
    public RateLimitService inMemoryRateLimitService() {
        log.info("Creating InMemoryRateLimitService bean");
        return new InMemoryRateLimitService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "api-throttle", name = "default-storage", havingValue = "redis")
    @ConditionalOnClass(RedisTemplate.class)
    public RateLimitService redisRateLimitService(RedisTemplate<String, String> redisTemplate) {
        log.info("Creating RedisRateLimitService bean");
        return new RedisRateLimitService(redisTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ThrottleManager throttleManager(QuotaService quotaService, RateLimitService rateLimitService) {
        log.info("Creating ThrottleManager bean with quotaService={}, rateLimitService={}", 
                quotaService.getClass().getSimpleName(), rateLimitService.getClass().getSimpleName());
        return new ThrottleManager(rateLimitService, quotaService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService, RequestKeyGenerator requestKeyGenerator) {
        log.info("Creating RateLimitAspect bean");
        return new RateLimitAspect(rateLimitService, requestKeyGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiQuotaAspect apiQuotaAspect(QuotaService quotaService, RequestKeyGenerator requestKeyGenerator) {
        log.info("Creating ApiQuotaAspect bean");
        return new ApiQuotaAspect(quotaService, requestKeyGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottleResponseAdvice throttleResponseAdvice(RateLimitService rateLimitService, QuotaService quotaService, RequestKeyGenerator requestKeyGenerator) {
        log.info("Creating ThrottleResponseAdvice bean");
        return new ThrottleResponseAdvice(rateLimitService, quotaService, requestKeyGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottleExceptionHandler throttleExceptionHandler() {
        log.info("Creating ThrottleExceptionHandler bean");
        return new ThrottleExceptionHandler();
    }
} 