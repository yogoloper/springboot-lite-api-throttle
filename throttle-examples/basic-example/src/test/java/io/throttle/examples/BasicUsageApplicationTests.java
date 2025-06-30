package io.throttle.examples;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BasicUsageApplication.class
)
@TestPropertySource(properties = {
    "spring.main.web-application-type=servlet",
    "spring.autoconfigure.exclude="
})
class BasicUsageApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void contextLoads() {
        // Spring Boot 애플리케이션이 정상적으로 로드되는지 확인
        System.out.println("=== Context Load Test ===");
        System.out.println("Available beans: " + applicationContext.getBeanDefinitionNames().length);
        
        // AOP 관련 빈들이 등록되었는지 확인
        try {
            Object rateLimitAspect = applicationContext.getBean("rateLimitAspect");
            System.out.println("RateLimitAspect found: " + rateLimitAspect.getClass().getName());
        } catch (Exception e) {
            System.out.println("RateLimitAspect not found: " + e.getMessage());
        }
        
        try {
            Object apiQuotaAspect = applicationContext.getBean("apiQuotaAspect");
            System.out.println("ApiQuotaAspect found: " + apiQuotaAspect.getClass().getName());
        } catch (Exception e) {
            System.out.println("ApiQuotaAspect not found: " + e.getMessage());
        }
        
        try {
            Object throttleResponseAdvice = applicationContext.getBean("throttleResponseAdvice");
            System.out.println("ThrottleResponseAdvice found: " + throttleResponseAdvice.getClass().getName());
        } catch (Exception e) {
            System.out.println("ThrottleResponseAdvice not found: " + e.getMessage());
        }
    }

    @Test
    void testUnlimitedEndpoint() {
        // 제한 없는 엔드포인트 테스트
        System.out.println("=== Testing Unlimited Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/unlimited", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("Response headers: " + response.getHeaders());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Unlimited data"));
    }

    @Test
    void testRateLimitEndpoint() {
        // Rate Limit 엔드포인트 테스트 (5번 요청 후 6번째는 429)
        System.out.println("=== Testing Rate Limit Endpoint ===");
        String url = "http://localhost:" + port + "/api/public";
        
        // 처음 5번은 성공
        for (int i = 0; i < 5; i++) {
            System.out.println("Request " + (i + 1) + ":");
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("  Status: " + response.getStatusCode());
            System.out.println("  Body: " + response.getBody());
            System.out.println("  Headers: " + response.getHeaders());
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        
        // 6번째 요청 (초과)
        System.out.println("Request 6 (should be 429):");
        ResponseEntity<String> response6 = restTemplate.getForEntity(url, String.class);
        System.out.println("  Status: " + response6.getStatusCode());
        System.out.println("  Body: " + response6.getBody());
        System.out.println("  Headers: " + response6.getHeaders());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response6.getStatusCode());
        
        // Rate Limit 헤더 확인
        assertNotNull(response6.getHeaders().get("X-RateLimit-Limit"));
        assertNotNull(response6.getHeaders().get("X-RateLimit-Remaining"));
        assertNotNull(response6.getHeaders().get("Retry-After"));
    }

    @Test
    void testQuotaOnlyEndpoint() {
        // Quota만 적용된 엔드포인트 테스트
        System.out.println("=== Testing Quota Only Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/quota-only", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("Response headers: " + response.getHeaders());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Quota only data"));
        
        // Quota 헤더 확인
        assertNotNull(response.getHeaders().get("X-Quota-Daily-Limit"));
        assertNotNull(response.getHeaders().get("X-Quota-Daily-Remaining"));
    }

    @Test
    void testDualProtectionEndpoint() {
        // Rate Limit + Quota 동시 적용 엔드포인트 테스트
        System.out.println("=== Testing Dual Protection Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/user-data", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("Response headers: " + response.getHeaders());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Rate limited + Quota applied"));
        
        // Rate Limit + Quota 헤더 모두 확인
        assertNotNull(response.getHeaders().get("X-RateLimit-Limit"));
        assertNotNull(response.getHeaders().get("X-Quota-Daily-Limit"));
    }
} 