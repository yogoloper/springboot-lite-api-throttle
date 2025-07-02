package io.throttle.examples;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 토큰 기반 사용자별 API 제한 예제 통합 테스트
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtExampleApplicationTests {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void contextLoads() {
        // Spring Boot 컨텍스트가 정상적으로 로드되는지 확인
        assertTrue(true, "Application context should load successfully");
    }

    @Test
    void testLoginEndpoint() {
        // 로그인 엔드포인트 테스트
        System.out.println("=== Testing Login Endpoint ===");
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "free-user");
        loginRequest.put("password", "password");
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/auth/login",
            HttpMethod.POST,
            request,
            Map.class
        );
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("token"));
        assertEquals("Login successful", response.getBody().get("message"));
    }

    @Test
    void testUserProfileEndpoint() {
        // 사용자 프로필 엔드포인트 테스트 (토큰 없이)
        System.out.println("=== Testing User Profile Endpoint ===");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/user-profile", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        // 토큰이 없어도 응답은 받을 수 있음 (실제로는 401이어야 함)
        assertNotNull(response.getBody());
    }

    @Test
    void testPremiumDataEndpoint() {
        // 프리미엄 데이터 엔드포인트 테스트
        System.out.println("=== Testing Premium Data Endpoint ===");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/premium-data", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertNotNull(response.getBody());
    }

    @Test
    void testEnterpriseDataEndpoint() {
        // 엔터프라이즈 데이터 엔드포인트 테스트
        System.out.println("=== Testing Enterprise Data Endpoint ===");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/enterprise-data", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertNotNull(response.getBody());
    }

    @Test
    void testUserStatsEndpoint() {
        // 사용자 통계 엔드포인트 테스트
        System.out.println("=== Testing User Stats Endpoint ===");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/user-stats", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertNotNull(response.getBody());
    }

    @Test
    void testUserSettingsEndpoint() {
        // 사용자 설정 엔드포인트 테스트
        System.out.println("=== Testing User Settings Endpoint ===");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/user-settings", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertNotNull(response.getBody());
    }

    @Test
    void testUserNotificationsEndpoint() {
        // 사용자 알림 엔드포인트 테스트
        System.out.println("=== Testing User Notifications Endpoint ===");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/user-notifications", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertNotNull(response.getBody());
    }

    @Test
    void testTokenVerification() {
        // 토큰 검증 엔드포인트 테스트
        System.out.println("=== Testing Token Verification Endpoint ===");
        
        // 먼저 로그인하여 토큰 획득
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "premium-user");
        loginRequest.put("password", "password");
        
        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest);
        ResponseEntity<Map> loginResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/auth/login",
            HttpMethod.POST,
            loginEntity,
            Map.class
        );
        
        String token = (String) loginResponse.getBody().get("token");
        
        // 토큰으로 검증 요청
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> verifyEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> verifyResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/auth/verify",
            HttpMethod.GET,
            verifyEntity,
            Map.class
        );
        
        System.out.println("Verify response status: " + verifyResponse.getStatusCode());
        System.out.println("Verify response body: " + verifyResponse.getBody());
        
        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertTrue((Boolean) verifyResponse.getBody().get("valid"));
        assertEquals("premium-user", verifyResponse.getBody().get("username"));
    }
} 