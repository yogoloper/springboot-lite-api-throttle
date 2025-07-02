package io.throttle.examples.controller;

import io.throttle.spring.annotation.ApiQuota;
import io.throttle.spring.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 환경에서의 API 제한 예제
 * 
 * 이 컨트롤러는 Redis를 사용하여 여러 서버 인스턴스 간에
 * Rate Limit과 Quota 정보를 공유하는 방법을 보여줍니다.
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class DistributedApiController {

    /**
     * 분산 환경에서의 Rate Limit
     * 여러 서버 인스턴스에서 공유되는 Rate Limit
     * 1분에 20번만 요청 가능 (전체 인스턴스 합계)
     */
    @RateLimit(requests = 20, per = TimeUnit.MINUTES, key = "ip")
    @GetMapping("/distributed")
    public ResponseEntity<String> getDistributedData() {
        return ResponseEntity.ok("Distributed data - Rate limited across all instances (20/min)");
    }

    /**
     * 분산 환경에서의 Quota
     * 여러 서버 인스턴스에서 공유되는 일일/월간 Quota
     * 하루 500번, 한 달 10000번 제한 (전체 인스턴스 합계)
     */
    @ApiQuota(daily = 500, monthly = 10000, key = "ip")
    @GetMapping("/distributed-quota")
    public ResponseEntity<String> getDistributedQuotaData() {
        return ResponseEntity.ok("Distributed quota data - Daily: 500, Monthly: 10000 (shared across instances)");
    }

    /**
     * 이중 보호 체계 (Rate Limit + Quota)
     * 분산 환경에서 Rate Limit과 Quota를 동시에 적용
     */
    @RateLimit(requests = 10, per = TimeUnit.MINUTES, key = "ip")
    @ApiQuota(daily = 200, monthly = 5000, key = "ip")
    @GetMapping("/distributed-dual")
    public ResponseEntity<String> getDistributedDualData() {
        return ResponseEntity.ok("Distributed dual protection - Rate limit (10/min) + Quota (daily: 200, monthly: 5000)");
    }

    /**
     * 사용자별 분산 제한
     * JWT 토큰 기반 사용자별 Rate Limit (분산 환경)
     */
    @RateLimit(requests = 50, per = TimeUnit.HOURS, key = "user")
    @ApiQuota(daily = 1000, monthly = 20000, key = "user")
    @GetMapping("/distributed-user")
    public ResponseEntity<String> getDistributedUserData() {
        return ResponseEntity.ok("Distributed user data - User-based limits across all instances");
    }

    /**
     * API 키 기반 분산 제한
     * 특정 헤더 값 기반 Rate Limit (분산 환경)
     */
    @RateLimit(requests = 5, per = TimeUnit.MINUTES, key = "header:X-API-Key")
    @GetMapping("/distributed-api-key")
    public ResponseEntity<String> getDistributedApiKeyData() {
        return ResponseEntity.ok("Distributed API key data - Header-based limits across all instances");
    }
} 