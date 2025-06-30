package io.throttle.examples.controller;

import io.throttle.spring.annotation.ApiQuota;
import io.throttle.spring.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * 기본 Rate Limit만 적용
     * 1분에 5번만 요청 가능
     */
    @RateLimit(requests = 5, per = TimeUnit.MINUTES)
    @GetMapping("/public")
    public ResponseEntity<String> getPublicData() {
        return ResponseEntity.ok("Public data - Rate limited to 5 requests per minute");
    }

    /**
     * Rate Limit + Quota 동시 적용
     * 1분에 10번, 하루 100번, 한 달 1000번 제한
     */
    @RateLimit(requests = 10, per = TimeUnit.MINUTES)
    @ApiQuota(daily = 100, monthly = 1000)
    @GetMapping("/user-data")
    public ResponseEntity<String> getUserData() {
        return ResponseEntity.ok("User data - Rate limited + Quota applied");
    }

    /**
     * Quota만 적용
     * 하루 50번, 한 달 500번 제한
     */
    @ApiQuota(daily = 50, monthly = 500)
    @GetMapping("/quota-only")
    public ResponseEntity<String> getQuotaOnlyData() {
        return ResponseEntity.ok("Quota only data - Daily: 50, Monthly: 500");
    }

    /**
     * 제한 없는 엔드포인트
     */
    @GetMapping("/unlimited")
    public ResponseEntity<String> getUnlimitedData() {
        return ResponseEntity.ok("Unlimited data - No restrictions");
    }
} 