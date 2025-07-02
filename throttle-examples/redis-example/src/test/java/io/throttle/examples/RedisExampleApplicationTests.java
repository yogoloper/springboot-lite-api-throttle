package io.throttle.examples;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 기반 분산 환경 예제 통합 테스트
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RedisExampleApplicationTests {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    // Redis 컨테이너 설정
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void contextLoads() {
        // Spring Boot 컨텍스트가 정상적으로 로드되는지 확인
        assertTrue(true, "Application context should load successfully");
    }

    @Test
    void testDistributedEndpoint() {
        // 분산 환경 Rate Limit 엔드포인트 테스트
        System.out.println("=== Testing Distributed Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/distributed", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("Response headers: " + response.getHeaders());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Distributed data"));
        
        // Rate Limit 헤더 확인
        assertNotNull(response.getHeaders().get("X-RateLimit-Limit"));
        assertNotNull(response.getHeaders().get("X-RateLimit-Remaining"));
    }

    @Test
    void testDistributedQuotaEndpoint() {
        // 분산 환경 Quota 엔드포인트 테스트
        System.out.println("=== Testing Distributed Quota Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/distributed-quota", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Distributed quota data"));
        
        // Quota 헤더 확인
        assertNotNull(response.getHeaders().get("X-Quota-Daily-Limit"));
        assertNotNull(response.getHeaders().get("X-Quota-Monthly-Limit"));
    }

    @Test
    void testDistributedDualProtectionEndpoint() {
        // 분산 환경 이중 보호 체계 엔드포인트 테스트
        System.out.println("=== Testing Distributed Dual Protection Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/distributed-dual", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Distributed dual protection"));
        
        // Rate Limit + Quota 헤더 모두 확인
        assertNotNull(response.getHeaders().get("X-RateLimit-Limit"));
        assertNotNull(response.getHeaders().get("X-Quota-Daily-Limit"));
    }

    @Test
    void testDistributedUserEndpoint() {
        // 분산 환경 사용자별 제한 엔드포인트 테스트
        System.out.println("=== Testing Distributed User Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/distributed-user", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Distributed user data"));
    }

    @Test
    void testDistributedApiKeyEndpoint() {
        // 분산 환경 API 키 기반 제한 엔드포인트 테스트
        System.out.println("=== Testing Distributed API Key Endpoint ===");
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/distributed-api-key", String.class);
        
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Distributed API key data"));
    }
} 