package io.throttle.spring.starter.aspect;

import io.throttle.core.api.RateLimitService;
import io.throttle.core.exception.RateLimitExceededException;
import io.throttle.core.model.RateLimit;
import io.throttle.spring.starter.RequestKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * RateLimit 애노테이션을 처리하는 AOP Aspect
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimitService rateLimitService;
    private final RequestKeyGenerator requestKeyGenerator;

    @Around("@annotation(rateLimitAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, io.throttle.spring.annotation.RateLimit rateLimitAnnotation) throws Throwable {
        log.info("=== RateLimitAspect triggered ===");
        log.info("Method: {}", joinPoint.getSignature().getName());
        log.info("Rate limit annotation: requests={}, per={}", 
                 rateLimitAnnotation.requests(), rateLimitAnnotation.per());
        
        String key = requestKeyGenerator.generateKey();
        
        log.info("Rate limit check for key: {}, limit: {} requests per {}", 
                 key, rateLimitAnnotation.requests(), rateLimitAnnotation.per());
        
        try {
            // Rate limit 체크
            RateLimit rateLimit = new RateLimit(
                rateLimitAnnotation.requests(), 
                Duration.of(1, rateLimitAnnotation.per().toChronoUnit()),
                key
            );
            
            log.info("Checking rate limit: {}", rateLimit);
            rateLimitService.checkLimit(key, rateLimit);
            log.info("Rate limit check passed");
            
            // 요청 처리
            Object result = joinPoint.proceed();
            
            // 성공 시 카운트 증가
            log.info("Incrementing rate limit counter");
            rateLimitService.increment(key, rateLimit);
            log.info("Rate limit counter incremented");
            
            return result;
            
        } catch (RateLimitExceededException e) {
            log.info("Rate limit exceeded for key: {}", key);
            throw e;
        } catch (Exception e) {
            log.error("Error in RateLimitAspect: {}", e.getMessage(), e);
            throw e;
        }
    }
} 