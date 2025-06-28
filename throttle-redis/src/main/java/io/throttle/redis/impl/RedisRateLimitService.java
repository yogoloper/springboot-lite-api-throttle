package io.throttle.redis.impl;

import io.throttle.core.exception.RateLimitExceededException;
import io.throttle.core.model.RateLimit;
import io.throttle.core.api.RateLimitService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * Redis 기반 Rate Limit 서비스 구현체
 */
@Service
public class RedisRateLimitService implements RateLimitService {
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> checkAndIncrementScript;

    public RedisRateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.checkAndIncrementScript = RedisScript.of(
            // KEYS[1] - rate limit key
            // ARGV[1] - window size in seconds
            // ARGV[2] - max requests per window
            "local current = redis.call('GET', KEYS[1])\n" +
            "if current == false then\n" +
            "    redis.call('SET', KEYS[1], 1, 'EX', ARGV[1])\n" +
            "    return 1\n" +
            "else\n" +
            "    local newCount = tonumber(current) + 1\n" +
            "    if newCount > tonumber(ARGV[2]) then\n" +
            "        return -1\n" +
            "    else\n" +
            "        redis.call('INCR', KEYS[1])\n" +
            "        return newCount\n" +
            "    end\n" +
            "end",
            Long.class
        );
    }

    @Override
    public void checkLimit(String key, RateLimit rateLimit) {
        String redisKey = buildRedisKey(key, rateLimit);
        
        // -1 means rate limit exceeded
        Long result = redisTemplate.execute(
            checkAndIncrementScript,
            Collections.singletonList(redisKey),
            String.valueOf(rateLimit.getDuration().getSeconds()),
            String.valueOf(rateLimit.getLimit())
        );
        
        if (result != null && result == -1L) {
            long remainingTime = getRemainingTime(redisKey, rateLimit);
            throw new RateLimitExceededException(
                "Rate limit exceeded. Try again in " + remainingTime + " seconds",
                rateLimit,
                0, // No remaining requests
                remainingTime
            );
        }
    }

    @Override
    public void increment(String key, RateLimit rateLimit) {
        // No-op, increment is now handled in checkLimit
    }

    @Override
    public int getRemaining(String key, RateLimit rateLimit) {
        String redisKey = buildRedisKey(key, rateLimit);
        Long current = getCurrentCount(redisKey);
        return (int) Math.max(0, rateLimit.getLimit() - current);
    }

    @Override
    public long getRemainingTime(String key, RateLimit rateLimit) {
        String redisKey = buildRedisKey(key, rateLimit);
        Long ttl = redisTemplate.getExpire(redisKey);
        return ttl != null ? ttl : 0;
    }

    private Long getCurrentCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    private String buildRedisKey(String key, RateLimit rateLimit) {
        return RATE_LIMIT_KEY_PREFIX + rateLimit.getKey() + ":" + key;
    }
}