package io.throttle.examples.controller;

import io.throttle.spring.annotation.ApiQuota;
import io.throttle.spring.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 기반 사용자별 API 제한 예제
 * 
 * 이 컨트롤러는 JWT 토큰을 사용하여 사용자별로 다른 Rate Limit과 Quota를
 * 적용하는 방법을 보여줍니다.
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class UserApiController {

    /**
     * 사용자 프로필 조회 (사용자별 제한)
     * FREE: 1분에 10번, 하루 100번
     * PREMIUM: 1분에 50번, 하루 1000번
     * ENTERPRISE: 1분에 200번, 하루 10000번
     */
    @RateLimit(requests = 10, per = TimeUnit.MINUTES, key = "user")
    @ApiQuota(daily = 100, monthly = 3000, key = "user")
    @GetMapping("/user-profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        // JWT 토큰에서 사용자 정보 추출 (실제로는 인터셉터에서 처리)
        String token = authHeader.replace("Bearer ", "");
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("message", "User profile retrieved successfully");
        profile.put("userType", "FREE"); // 실제로는 JWT에서 추출
        profile.put("rateLimit", "10 requests per minute");
        profile.put("dailyQuota", "100 requests per day");
        
        return ResponseEntity.ok(profile);
    }

    /**
     * 프리미엄 데이터 조회 (높은 제한)
     * PREMIUM 이상 사용자만 접근 가능
     */
    @RateLimit(requests = 50, per = TimeUnit.MINUTES, key = "user")
    @ApiQuota(daily = 1000, monthly = 30000, key = "user")
    @GetMapping("/premium-data")
    public ResponseEntity<Map<String, Object>> getPremiumData(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Premium data retrieved successfully");
        data.put("userType", "PREMIUM");
        data.put("rateLimit", "50 requests per minute");
        data.put("dailyQuota", "1000 requests per day");
        
        return ResponseEntity.ok(data);
    }

    /**
     * 엔터프라이즈 데이터 조회 (최고 제한)
     * ENTERPRISE 사용자만 접근 가능
     */
    @RateLimit(requests = 200, per = TimeUnit.MINUTES, key = "user")
    @ApiQuota(daily = 10000, monthly = 300000, key = "user")
    @GetMapping("/enterprise-data")
    public ResponseEntity<Map<String, Object>> getEnterpriseData(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Enterprise data retrieved successfully");
        data.put("userType", "ENTERPRISE");
        data.put("rateLimit", "200 requests per minute");
        data.put("dailyQuota", "10000 requests per day");
        
        return ResponseEntity.ok(data);
    }

    /**
     * 사용자별 통계 조회 (이중 보호 체계)
     * Rate Limit + Quota 동시 적용
     */
    @RateLimit(requests = 30, per = TimeUnit.MINUTES, key = "user")
    @ApiQuota(daily = 500, monthly = 15000, key = "user")
    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "User statistics retrieved successfully");
        stats.put("rateLimit", "30 requests per minute");
        stats.put("dailyQuota", "500 requests per day");
        stats.put("monthlyQuota", "15000 requests per month");
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 사용자별 설정 조회 (낮은 제한)
     * 모든 사용자 타입에 대해 동일한 제한
     */
    @RateLimit(requests = 5, per = TimeUnit.MINUTES, key = "user")
    @ApiQuota(daily = 50, monthly = 1500, key = "user")
    @GetMapping("/user-settings")
    public ResponseEntity<Map<String, Object>> getUserSettings(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("message", "User settings retrieved successfully");
        settings.put("rateLimit", "5 requests per minute");
        settings.put("dailyQuota", "50 requests per day");
        
        return ResponseEntity.ok(settings);
    }

    /**
     * 사용자별 알림 조회 (사용자 기반 제한)
     * 사용자 기반 Rate Limit과 Quota 적용
     */
    @RateLimit(requests = 100, per = TimeUnit.HOURS, key = "user")
    @ApiQuota(daily = 200, monthly = 6000, key = "user")
    @GetMapping("/user-notifications")
    public ResponseEntity<Map<String, Object>> getUserNotifications(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> notifications = new HashMap<>();
        notifications.put("message", "User notifications retrieved successfully");
        notifications.put("userRateLimit", "100 requests per hour");
        notifications.put("dailyQuota", "200 requests per day");
        
        return ResponseEntity.ok(notifications);
    }
} 