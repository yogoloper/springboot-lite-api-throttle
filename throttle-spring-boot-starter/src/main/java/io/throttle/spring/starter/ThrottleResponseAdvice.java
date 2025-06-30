package io.throttle.spring.starter;

import io.throttle.core.api.QuotaService;
import io.throttle.core.api.RateLimitService;
import io.throttle.core.model.Quota;
import io.throttle.core.model.RateLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Instant;

/**
 * Rate Limiting 관련 HTTP 헤더를 응답에 추가하는 Advice
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ThrottleResponseAdvice implements ResponseBodyAdvice<Object> {

    private final RateLimitService rateLimitService;
    private final QuotaService quotaService;
    private final RequestKeyGenerator requestKeyGenerator;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 모든 REST 컨트롤러 응답에 적용
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {
        
        try {
            String key = requestKeyGenerator.generateKey();
            String endpoint = request.getURI().getPath();
            
            log.info("=== ThrottleResponseAdvice triggered for endpoint: {} ===", endpoint);
            
            // Rate Limit 헤더 추가
            addRateLimitHeaders(response, key, endpoint);
            
            // Quota 헤더 추가
            addQuotaHeaders(response, key, endpoint);
            
            log.info("Throttle headers added successfully");
            
        } catch (Exception e) {
            log.info("Failed to add throttle headers: {}", e.getMessage());
        }
        
        return body;
    }

    /**
     * Rate Limit 관련 헤더 추가
     */
    private void addRateLimitHeaders(ServerHttpResponse response, String key, String endpoint) {
        try {
            // 기본 Rate Limit 정보 (1분에 100회로 가정)
            RateLimit defaultRateLimit = new RateLimit(100, java.time.Duration.ofMinutes(1), key);
            
            int limit = defaultRateLimit.getLimit();
            int remaining = rateLimitService.getRemaining(key, defaultRateLimit);
            long resetTime = Instant.now().plus(defaultRateLimit.getDuration()).getEpochSecond();
            
            response.getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
            response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
            response.getHeaders().add("X-RateLimit-Reset", String.valueOf(resetTime));
            
            log.info("Rate limit headers added: limit={}, remaining={}, reset={}", limit, remaining, resetTime);
            
        } catch (Exception e) {
            log.info("Failed to add rate limit headers: {}", e.getMessage());
        }
    }

    /**
     * Quota 관련 헤더 추가
     */
    private void addQuotaHeaders(ServerHttpResponse response, String key, String endpoint) {
        try {
            // 일일 Quota 정보 (1000회로 가정)
            Instant dailyResetTime = Instant.now().plusSeconds(86400).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            Quota dailyQuota = new Quota(1000, Quota.Period.DAILY, dailyResetTime, key + ":daily");
            
            int dailyLimit = dailyQuota.getLimit();
            int dailyRemaining = quotaService.getRemaining(key + ":daily", dailyQuota);
            long dailyReset = dailyQuota.getResetTime().getEpochSecond();
            
            response.getHeaders().add("X-Quota-Daily-Limit", String.valueOf(dailyLimit));
            response.getHeaders().add("X-Quota-Daily-Remaining", String.valueOf(dailyRemaining));
            response.getHeaders().add("X-Quota-Daily-Reset", String.valueOf(dailyReset));
            
            // 월간 Quota 정보 (30000회로 가정)
            Instant monthlyResetTime = Instant.now().plusSeconds(2592000).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            Quota monthlyQuota = new Quota(30000, Quota.Period.MONTHLY, monthlyResetTime, key + ":monthly");
            
            int monthlyLimit = monthlyQuota.getLimit();
            int monthlyRemaining = quotaService.getRemaining(key + ":monthly", monthlyQuota);
            long monthlyReset = monthlyQuota.getResetTime().getEpochSecond();
            
            response.getHeaders().add("X-Quota-Monthly-Limit", String.valueOf(monthlyLimit));
            response.getHeaders().add("X-Quota-Monthly-Remaining", String.valueOf(monthlyRemaining));
            response.getHeaders().add("X-Quota-Monthly-Reset", String.valueOf(monthlyReset));
            
            log.info("Quota headers added: daily(limit={}, remaining={}), monthly(limit={}, remaining={})", 
                    dailyLimit, dailyRemaining, monthlyLimit, monthlyRemaining);
            
        } catch (Exception e) {
            log.info("Failed to add quota headers: {}", e.getMessage());
        }
    }
} 