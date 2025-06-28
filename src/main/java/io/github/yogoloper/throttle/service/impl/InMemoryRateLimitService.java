package io.github.yogoloper.throttle.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import io.github.yogoloper.throttle.exception.RateLimitExceededException;
import io.github.yogoloper.throttle.model.RateLimit;
import io.github.yogoloper.throttle.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 메모리 기반 요청 속도 제한 서비스 구현체
 */
@Service
public class InMemoryRateLimitService implements RateLimitService {

    // 요청 카운트를 저장할 캐시
    // 키: rateLimitKey, 값: 현재 요청 수
    private final ConcurrentMap<String, Integer> requestCounts = new ConcurrentHashMap<>();
    
    // 마지막 리셋 시간을 추적하는 맵 (초 단위 타임스탬프)
    private final ConcurrentMap<String, Long> resetTimes = new ConcurrentHashMap<>();
    
    @Override
    public void checkLimit(String key, RateLimit rateLimit) throws RateLimitExceededException {
        String rateLimitKey = buildRateLimitKey(key, rateLimit);
        long now = System.currentTimeMillis() / 1000; // 초 단위
        long windowSize = rateLimit.getDuration().getSeconds();
        long currentWindow = now / windowSize;
        
        // 윈도우가 변경되었는지 확인하고, 변경되었다면 카운트 초기화
        resetIfNeeded(rateLimitKey, currentWindow, windowSize);
        
        // 현재 윈도우의 요청 수 가져오기 (없으면 0으로 초기화)
        int currentCount = requestCounts.getOrDefault(rateLimitKey, 0);
        
        // 제한 초과 시 예외 발생
        if (currentCount >= rateLimit.getLimit()) {
            long remainingTime = calculateRemainingTime(now, windowSize);
            throw new RateLimitExceededException(
                "Rate limit exceeded. Try again in " + remainingTime + " seconds",
                remainingTime,
                rateLimit.getLimit(),
                0,
                java.time.Instant.ofEpochSecond((currentWindow + 1) * windowSize)
            );
        }
    }
    
    @Override
    public void increment(String key, RateLimit rateLimit) {
        String rateLimitKey = buildRateLimitKey(key, rateLimit);
        long now = System.currentTimeMillis() / 1000; // 초 단위
        long windowSize = rateLimit.getDuration().getSeconds();
        long currentWindow = now / windowSize;
        
        // 윈도우가 변경되었는지 확인하고, 변경되었다면 카운트 초기화
        resetIfNeeded(rateLimitKey, currentWindow, windowSize);
        
        // 요청 수 증가 (동시성 보장을 위해 compute 사용)
        requestCounts.compute(rateLimitKey, (k, v) -> (v == null) ? 1 : v + 1);
    }
    
    @Override
    public int getRemaining(String key, RateLimit rateLimit) {
        String rateLimitKey = buildRateLimitKey(key, rateLimit);
        int currentCount = requestCounts.getOrDefault(rateLimitKey, 0);
        return Math.max(0, rateLimit.getLimit() - currentCount);
    }
    
    @Override
    public long getRemainingTime(String key, RateLimit rateLimit) {
        long now = System.currentTimeMillis() / 1000; // 초 단위
        long windowSize = rateLimit.getDuration().getSeconds();
        return calculateRemainingTime(now, windowSize);
    }
    
    // 비공개 헬퍼 메서드들
    
    /**
     * Rate Limit 키를 생성합니다.
     * 형식: "{key}:{durationInSeconds}"
     */
    private String buildRateLimitKey(String key, RateLimit rateLimit) {
        return String.format("%s:%s:%d", 
            key, 
            rateLimit.getKey(), 
            rateLimit.getDuration().getSeconds());
    }
    
    /**
     * 현재 윈도우가 변경되었는지 확인하고, 변경되었다면 카운트를 초기화합니다.
     */
    private void resetIfNeeded(String rateLimitKey, long currentWindow, long windowSize) {
        resetTimes.compute(rateLimitKey, (k, v) -> {
            if (v == null || v < currentWindow) {
                // 새 윈도우 시작: 카운트 초기화
                requestCounts.put(rateLimitKey, 0);
                return currentWindow;
            }
            return v; // 기존 윈도우 유지
        });
    }
    
    /**
     * 다음 윈도우까지 남은 시간을 계산합니다.
     */
    private long calculateRemainingTime(long now, long windowSize) {
        return ((now / windowSize) + 1) * windowSize - now;
    }
}
