package io.throttle.redis.impl;

import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.model.Quota;
import io.throttle.core.api.QuotaService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * Redis 기반 할당량 관리 서비스 구현체
 */
@Service
public class RedisQuotaService implements QuotaService {

    private static final String QUOTA_KEY_PREFIX = "quota:";
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> quotaScript;

    public RedisQuotaService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.quotaScript = RedisScript.of(
            // Lua 스크립트: 할당량 증가 및 제한 확인
            "local resetKey = KEYS[1] .. ':reset'\n" +
            "local reset = redis.call('get', resetKey)\n" +
            "local now = tonumber(redis.call('time')[1])\n" +
            "local resetTime = tonumber(ARGV[1])\n" +
            "local limit = tonumber(ARGV[2])\n" +
            // 키가 없을 때만 0으로 초기화
            "redis.call('setnx', KEYS[1], 0)\n" +
            // reset이 없거나, reset < now(즉, 만료 시점이 지남)
            "if (not reset) or (tonumber(reset) < now) then\n" +
            "    redis.call('set', KEYS[1], 0)\n" +
            "    -- resetTime이 반드시 now보다 미래여야만 resetKey를 갱신\n" +
            "    if resetTime > now then\n" +
            "        redis.call('set', resetKey, resetTime)\n" +
            "    end\n" +
            "end\n" +
            // incr로 카운트 증가
            "local current = redis.call('incr', KEYS[1])\n" +
            // 제한을 초과하면 -1 반환 (예외 발생을 위한 신호)
            "if current > limit then\n" +
            "    return -1\n" +
            "end\n" +
            "return current\n",
            Long.class
        );
    }

    /**
     * 현재 할당량을 검증만 수행합니다. (카운트 증가 없음)
     * 할당량을 초과하면 예외를 발생시킵니다.
     */
    @Override
    public void checkQuota(String key, Quota quota) throws QuotaExceededException {
        String quotaKey = buildQuotaKey(key, quota);
        String currentStr = redisTemplate.opsForValue().get(quotaKey);
        int current = 0;
        if (currentStr != null) {
            try {
                current = Integer.parseInt(currentStr);
            } catch (NumberFormatException ignored) {}
        }
        if (current >= quota.getLimit()) {
            throw new QuotaExceededException(
                "Quota exceeded. Try again after " + quota.getResetTime() +
                " (" + quota.getPeriod() + " limit: " + quota.getLimit() + ")",
                quota.getPeriod().name().toLowerCase(),
                quota.getLimit(),
                0,
                quota.getResetTime()
            );
        }
    }

    /**
     * 실제로 할당량을 1 증가시키고, 리셋 시간도 관리합니다. (atomic 보장)
     * 제한을 초과하면 예외를 발생시킵니다.
     */
    @Override
    public void useQuota(String key, Quota quota) throws QuotaExceededException {
        String quotaKey = buildQuotaKey(key, quota);
        long resetTime = getResetTime(quota).getEpochSecond();
        Long result = redisTemplate.execute(
            quotaScript,
            Collections.singletonList(quotaKey),
            String.valueOf(resetTime),
            String.valueOf(quota.getLimit())
        );
        
        // Lua 스크립트에서 -1을 반환하면 제한 초과
        if (result != null && result == -1) {
            throw new QuotaExceededException(
                "Quota exceeded. Try again after " + quota.getResetTime() +
                " (" + quota.getPeriod() + " limit: " + quota.getLimit() + ")",
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
        String currentStr = redisTemplate.opsForValue().get(quotaKey);
        
        if (currentStr == null) {
            return quota.getLimit();
        }
        
        try {
            int current = Integer.parseInt(currentStr);
            return Math.max(0, quota.getLimit() - current);
        } catch (NumberFormatException e) {
            return quota.getLimit();
        }
    }

    @Override
    public long getRemainingTime(String key, Quota quota) {
        Instant now = Instant.now();
        if (now.isAfter(quota.getResetTime())) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(now, quota.getResetTime());
    }

    private String buildQuotaKey(String key, Quota quota) {
        return QUOTA_KEY_PREFIX + key + ":" + quota.getKey() + ":" + quota.getPeriod().name();
    }

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