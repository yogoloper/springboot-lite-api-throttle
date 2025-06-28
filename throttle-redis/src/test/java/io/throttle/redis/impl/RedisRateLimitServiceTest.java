package io.throttle.redis.impl;

import io.throttle.core.exception.RateLimitExceededException;
import io.throttle.core.model.RateLimit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

import io.throttle.redis.config.RedisTestConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {RedisTestConfig.class})
@Testcontainers
class RedisRateLimitServiceTest {
    
    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", (java.util.function.Supplier<Object>) () -> redis.getHost());
        registry.add("spring.redis.port", (java.util.function.Supplier<Object>) () -> redis.getMappedPort(6379).toString());
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private RedisRateLimitService rateLimitService;
    private static final String CLIENT_IP = "127.0.0.1";
    private static final String ENDPOINT = "test-endpoint";
    
    @BeforeEach
    void setUp() {
        // Redis 데이터 초기화
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }
        rateLimitService = new RedisRateLimitService(redisTemplate);
    }
    
    @AfterEach
    void tearDown() {
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }
    }

    @Test
    @DisplayName("요청 제한이 정확히 적용되어야 함")
    void testRateLimit() {
        // given: 1초에 3회 제한
        RateLimit rateLimit = new RateLimit(3, Duration.ofSeconds(1), ENDPOINT);
        
        // when: 3번 요청
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        }
        
        // then: 4번째 요청 시 예외 발생
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
        });
    }
    
    @Test
    @DisplayName("시간 경과 후 카운트 초기화가 정확히 적용되어야 함")
    void testRateLimitResetAfterTimeWindow() throws InterruptedException {
        // given: 1초에 3회 제한
        RateLimit rateLimit = new RateLimit(3, Duration.ofSeconds(1), ENDPOINT);
        
        // when: 3번 요청
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        }
        
        // then: 4번째 요청 시 예외 발생
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
        });
        
        // when: 1.1초 대기 (TTL 만료)
        TimeUnit.MILLISECONDS.sleep(1100);
        
        // then: 다시 요청 가능해야 함
        assertDoesNotThrow(() -> {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        });
    }
    
    @Test
    @DisplayName("동시 요청 테스트")
    void testConcurrentRequests() throws InterruptedException {
        // given: 10초에 100회 제한
        RateLimit rateLimit = new RateLimit(100, Duration.ofSeconds(10), ENDPOINT);
        int threadCount = 50;
        int requestsPerThread = 3; // 스레드당 3번 요청
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // when: 여러 스레드에서 동시에 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작할 수 있도록 대기
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            // checkLimit now also increments the counter
                            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
                            successCount.incrementAndGet();
                            
                            // 성공한 요청이 제한을 초과하면 테스트 실패
                            if (successCount.get() > rateLimit.getLimit()) {
                                throw new AssertionError("성공한 요청 수(" + successCount.get() + ")가 제한(" + rateLimit.getLimit() + ")을 초과함");
                            }
                        } catch (RateLimitExceededException e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        // 모든 스레드가 동시에 시작하도록 함
        startLatch.countDown();
        
        // then: 모든 스레드 완료 대기
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "테스트가 제한 시간 내에 완료되지 않았습니다.");
        executorService.shutdown();
        
        // then: 정확히 100개의 요청이 성공하고, 나머지는 실패해야 함
        assertEquals(100, successCount.get(), 
            "성공한 요청 수가 100개가 아닙니다. 실제: " + successCount.get());
        assertTrue(failureCount.get() >= 50, 
            "실패한 요청이 50개 미만입니다. 실제: " + failureCount.get());
        
        // 디버깅을 위해 결과 출력
        System.out.println("성공한 요청 수: " + successCount.get() + ", 실패한 요청 수: " + failureCount.get());
    }
    
    @Test
    @DisplayName("다른 키에 대한 요청은 독립적이어야 함")
    void testDifferentKeys() {
        // given: 1초에 3회 제한
        RateLimit rateLimit = new RateLimit(3, Duration.ofSeconds(1), ENDPOINT);
        String anotherClientIp = "192.168.0.1";
        
        // when: 첫 번째 클라이언트로 3번 요청
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        }
        
        // then: 첫 번째 클라이언트는 제한 초과
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
        });
        
        // then: 다른 클라이언트는 요청 가능
        assertDoesNotThrow(() -> {
            rateLimitService.checkLimit(anotherClientIp, rateLimit);
            rateLimitService.increment(anotherClientIp, rateLimit);
        });
    }
}