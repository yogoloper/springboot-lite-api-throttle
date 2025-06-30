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
        String key = requestKeyGenerator.generateKey();
        
        log.debug("Rate limit check for key: {}, limit: {} requests per {}", 
                 key, rateLimitAnnotation.requests(), rateLimitAnnotation.per());
        
        try {
            // Rate limit 체크
            RateLimit rateLimit = new RateLimit(
                rateLimitAnnotation.requests(), 
                Duration.of(1, rateLimitAnnotation.per().toChronoUnit()),
                key
            );
            rateLimitService.checkLimit(key, rateLimit);
            
            // 요청 처리
            Object result = joinPoint.proceed();
            
            // 성공 시 카운트 증가
            rateLimitService.increment(key, rateLimit);
            
            return result;
            
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw e;
        }
    }
} 