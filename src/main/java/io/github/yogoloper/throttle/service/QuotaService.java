package io.github.yogoloper.throttle.service;

import io.github.yogoloper.throttle.exception.QuotaExceededException;
import io.github.yogoloper.throttle.model.Quota;

/**
 * 할당량 관리 서비스 인터페이스
 */
public interface QuotaService {
    /**
     * 할당량을 확인
     * @param key 요청 식별 키
     * @param quota 할당량 정책
     * @throws QuotaExceededException 할당량을 초과한 경우
     */
    void checkQuota(String key, Quota quota) throws QuotaExceededException;

    /**
     * 할당량을 사용
     * @param key 요청 식별 키
     * @param quota 할당량 정책
     */
    void useQuota(String key, Quota quota);

    /**
     * 남은 할당량을 조회
     * @param key 요청 식별 키
     * @param quota 할당량 정책
     * @return 남은 할당량
     */
    int getRemaining(String key, Quota quota);

    /**
     * 남은 시간을 초 단위로 조회
     * @param key 요청 식별 키
     * @param quota 할당량 정책
     * @return 남은 시간 (초)
     */
    long getRemainingTime(String key, Quota quota);
}
