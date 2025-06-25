package io.github.yogoloper.throttle.service;

import io.github.yogoloper.throttle.model.RateLimit;
import io.github.yogoloper.throttle.exception.RateLimitExceededException;

/**
 * 요청 속도 제한 서비스 인터페이스
 */
public interface RateLimitService {
    /**
     * 요청을 처리하기 전에 제한을 확인
     * @param key 요청 식별 키
     * @param rateLimit 적용할 제한 정책
     * @throws RateLimitExceededException 제한을 초과한 경우
     */
    void checkLimit(String key, RateLimit rateLimit) throws RateLimitExceededException;

    /**
     * 요청을 처리한 후 카운트 증가
     * @param key 요청 식별 키
     * @param rateLimit 적용할 제한 정책
     */
    void increment(String key, RateLimit rateLimit);

    /**
     * 현재 남은 요청 수를 조회
     * @param key 요청 식별 키
     * @param rateLimit 적용할 제한 정책
     * @return 남은 요청 수
     */
    int getRemaining(String key, RateLimit rateLimit);

    /**
     * 남은 시간을 초 단위로 조회
     * @param key 요청 식별 키
     * @param rateLimit 적용할 제한 정책
     * @return 남은 시간 (초)
     */
    long getRemainingTime(String key, RateLimit rateLimit);
}
