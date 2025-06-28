package io.throttle.memory.impl;

import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.model.Quota;
import io.throttle.core.api.QuotaService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 메모리 기반 할당량 관리 서비스 구현체
 */
@Service
public class InMemoryQuotaService implements QuotaService {

    // 할당량 사용량을 저장할 캐시
    // 키: quotaKey, 값: 현재 사용량
    private final ConcurrentMap<String, AtomicInteger> quotaUsages = new ConcurrentHashMap<>();
    
    // 할당량 마지막 리셋 시간을 추적하는 맵 (초 단위 타임스탬프)
    private final ConcurrentMap<String, Long> quotaResetTimes = new ConcurrentHashMap<>();

    // InMemoryQuotaService.java에 다음 필드와 생성자 추가
    private final java.time.Clock clock;

    public InMemoryQuotaService() {
        this(java.time.Clock.systemDefaultZone());
    }

    // 테스트를 위한 생성자
    InMemoryQuotaService(java.time.Clock clock) {
        this.clock = clock;
    }

    // 기존 System.currentTimeMillis() / 1000 대신 clock.millis() / 1000 사용
    private long getCurrentTimeInSeconds() {
        return clock.millis() / 1000;
    }

    @Override
    public void checkQuota(String key, Quota quota) throws QuotaExceededException {
        String quotaKey = buildQuotaKey(key, quota);
        resetIfNeeded(quotaKey, quota);
        
        int currentUsage = getCurrentUsage(quotaKey);
        if (currentUsage >= quota.getLimit()) {
            throw new QuotaExceededException(
                "Quota exceeded. Try again after " + getResetTime(quota) + " (" + quota.getPeriod() + " limit: " + quota.getLimit() + ")",
                quota.getPeriod().name().toLowerCase(),
                quota.getLimit(),
                0,
                quota.getResetTime()
            );
        }
    }

    @Override
    public void useQuota(String key, Quota quota) throws QuotaExceededException {
        String quotaKey = buildQuotaKey(key, quota);
        resetIfNeeded(quotaKey, quota);
        
        int newUsage = quotaUsages.computeIfAbsent(quotaKey, k -> new AtomicInteger(0)).incrementAndGet();
        
        // 제한을 초과하면 예외 발생
        if (newUsage > quota.getLimit()) {
            throw new QuotaExceededException(
                "Quota exceeded. Try again after " + getResetTime(quota) + " (" + quota.getPeriod() + " limit: " + quota.getLimit() + ")",
                quota.getPeriod().name().toLowerCase(),
                quota.getLimit(),
                0,
                quota.getResetTime()
            );
        }
    }

    @Override
    public int getRemaining(String key, Quota quota) {
        String quotaKey = buildQuotaKey(key, quota);
        resetIfNeeded(quotaKey, quota);
        int currentUsage = getCurrentUsage(quotaKey);
        return Math.max(0, quota.getLimit() - currentUsage);
    }

    @Override
    public long getRemainingTime(String key, Quota quota) {
        Instant now = Instant.now();
        if (now.isAfter(quota.getResetTime())) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(now, quota.getResetTime());
    }

    /**
     * 할당량 키를 생성합니다.
     */
    private String buildQuotaKey(String key, Quota quota) {
        return String.format("%s:%s:%s", key, quota.getKey(), quota.getPeriod().name());
    }

    /**
     * 현재 사용량을 조회합니다.
     */
    private int getCurrentUsage(String quotaKey) {
        return quotaUsages.getOrDefault(quotaKey, new AtomicInteger(0)).get();
    }

    /**
     * 할당량을 초기화해야 하는 경우 초기화합니다.
     */
    private void resetIfNeeded(String quotaKey, Quota quota) {
        long now = System.currentTimeMillis() / 1000; // 초 단위
        quotaResetTimes.compute(quotaKey, (k, lastReset) -> {
            if (lastReset == null || isNewPeriod(lastReset, quota)) {
                // 새 기간 시작: 사용량 초기화
                quotaUsages.put(quotaKey, new AtomicInteger(0));
                return now;
            }
            return lastReset;
        });
    }

    /**
     * 새 기간이 시작되었는지 확인합니다.
     */
    private boolean isNewPeriod(long lastReset, Quota quota) {
        LocalDateTime lastResetTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(lastReset), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        
        if (quota.getPeriod() == Quota.Period.DAILY) {
            return lastResetTime.getDayOfYear() != now.getDayOfYear() || 
                   lastResetTime.getYear() != now.getYear();
        } else { // MONTHLY
            return lastResetTime.getMonth() != now.getMonth() || 
                   lastResetTime.getYear() != now.getYear();
        }
    }

    /**
     * 다음 리셋 시간을 계산합니다.
     */
    private Instant getResetTime(Quota quota) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetTime;
        
        if (quota.getPeriod() == Quota.Period.DAILY) {
            resetTime = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        } else { // MONTHLY
            resetTime = now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        
        return resetTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}