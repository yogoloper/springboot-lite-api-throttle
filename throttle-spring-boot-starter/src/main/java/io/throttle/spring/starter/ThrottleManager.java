package io.throttle.spring.starter;

import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.exception.RateLimitExceededException;
import io.throttle.core.model.Quota;
import io.throttle.core.model.RateLimit;
import io.throttle.core.api.QuotaService;
import io.throttle.core.api.RateLimitService;
import org.springframework.stereotype.Component;

/**
 * Rate Limiting과 Quota 관리를 통합하는 핵심 관리자 클래스
 */
@Component
public class ThrottleManager {
    
    private final RateLimitService rateLimitService;
    private final QuotaService quotaService;
    
    public ThrottleManager(RateLimitService rateLimitService, QuotaService quotaService) {
        this.rateLimitService = rateLimitService;
        this.quotaService = quotaService;
    }
    
    /**
     * Rate Limit과 Quota를 모두 확인합니다.
     * 
     * @param key 요청 식별 키
     * @param rateLimit Rate Limit 정책
     * @param quota Quota 정책
     * @throws RateLimitExceededException Rate Limit 초과 시
     * @throws QuotaExceededException Quota 초과 시
     */
    public void checkThrottle(String key, RateLimit rateLimit, Quota quota) 
            throws RateLimitExceededException, QuotaExceededException {
        // Rate Limit 먼저 확인 (더 빠른 응답)
        rateLimitService.checkLimit(key, rateLimit);
        
        // Quota 확인
        quotaService.checkQuota(key, quota);
    }
    
    /**
     * Rate Limit과 Quota를 모두 사용합니다.
     * 
     * @param key 요청 식별 키
     * @param rateLimit Rate Limit 정책
     * @param quota Quota 정책
     * @throws RateLimitExceededException Rate Limit 초과 시
     * @throws QuotaExceededException Quota 초과 시
     */
    public void useThrottle(String key, RateLimit rateLimit, Quota quota) 
            throws RateLimitExceededException, QuotaExceededException {
        // Rate Limit 사용
        rateLimitService.increment(key, rateLimit);
        
        // Quota 사용
        quotaService.useQuota(key, quota);
    }
    
    /**
     * Rate Limit만 확인합니다.
     */
    public void checkRateLimit(String key, RateLimit rateLimit) throws RateLimitExceededException {
        rateLimitService.checkLimit(key, rateLimit);
    }
    
    /**
     * Rate Limit만 사용합니다.
     */
    public void useRateLimit(String key, RateLimit rateLimit) throws RateLimitExceededException {
        rateLimitService.increment(key, rateLimit);
    }
    
    /**
     * Quota만 확인합니다.
     */
    public void checkQuota(String key, Quota quota) throws QuotaExceededException {
        quotaService.checkQuota(key, quota);
    }
    
    /**
     * Quota만 사용합니다.
     */
    public void useQuota(String key, Quota quota) throws QuotaExceededException {
        quotaService.useQuota(key, quota);
    }
    
    /**
     * Rate Limit 남은 요청 수를 조회합니다.
     */
    public int getRemainingRateLimit(String key, RateLimit rateLimit) {
        return rateLimitService.getRemaining(key, rateLimit);
    }
    
    /**
     * Quota 남은 요청 수를 조회합니다.
     */
    public int getRemainingQuota(String key, Quota quota) {
        return quotaService.getRemaining(key, quota);
    }
    
    /**
     * Rate Limit 리셋까지 남은 시간을 조회합니다.
     */
    public long getRemainingRateLimitTime(String key, RateLimit rateLimit) {
        return rateLimitService.getRemainingTime(key, rateLimit);
    }
    
    /**
     * Quota 리셋까지 남은 시간을 조회합니다.
     */
    public long getRemainingQuotaTime(String key, Quota quota) {
        return quotaService.getRemainingTime(key, quota);
    }
} 