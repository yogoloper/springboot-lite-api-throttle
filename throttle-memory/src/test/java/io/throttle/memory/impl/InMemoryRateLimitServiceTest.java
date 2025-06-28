package io.throttle.memory.impl;

import io.throttle.core.exception.RateLimitExceededException;
import io.throttle.core.model.RateLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InMemoryRateLimitService 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class InMemoryRateLimitServiceTest {

    private InMemoryRateLimitService rateLimitService;
    private static final String CLIENT_IP = "127.0.0.1";
    private static final String ENDPOINT = "test-endpoint";
    
    @BeforeEach
    void setUp() {
        rateLimitService = new InMemoryRateLimitService();
    }
    
    @Test
    @DisplayName("요청 제한이 정확히 적용되어야 함")
    void 요청_제한_테스트() {
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
    void 시간_경과_후_카운트_초기화_테스트() throws InterruptedException {
        // given: 1초에 3회 제한
        RateLimit rateLimit = new RateLimit(3, Duration.ofSeconds(1), ENDPOINT);
        
        // when: 3번 요청 후 1초 대기
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        }
        
        // then: 4번째 요청 시 예외 발생
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
        });
        
        // when: 1.1초 대기 (윈도우 시간 초과)
        Thread.sleep(1100);
        
        // then: 다시 요청 가능해야 함
        assertDoesNotThrow(() -> {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        });
    }
    
    @Test
    @DisplayName("동시_요청_테스트")
    void 동시_요청_테스트() throws InterruptedException {
        // given: 10초에 100회 제한
        RateLimit rateLimit = new RateLimit(100, Duration.ofSeconds(10), ENDPOINT);
        int threadCount = 50;
        int requestsPerThread = 3; // 스레드당 3번 요청
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // when: 여러 스레드에서 동시에 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
                            rateLimitService.increment(CLIENT_IP, rateLimit);
                            successCount.incrementAndGet();
                        } catch (RateLimitExceededException e) {
                            failureCount.incrementAndGet();
                            break;
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 테스트 완료 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // then: 성공한 요청 수가 제한을 초과하지 않아야 함
        System.out.println("성공한 요청: " + successCount.get());
        System.out.println("실패한 요청: " + failureCount.get());
        
        assertTrue(successCount.get() <= rateLimit.getLimit(), 
            "성공한 요청 수가 제한을 초과했습니다.");
        assertTrue(failureCount.get() >= 0);
    }
    
    @Test
    void 남은_요청_수_확인_테스트() {
        // given: 10초에 5회 제한
        RateLimit rateLimit = new RateLimit(5, Duration.ofSeconds(10), ENDPOINT);
        
        // when: 아무 요청도 하지 않았을 때
        int remaining = rateLimitService.getRemaining(CLIENT_IP, rateLimit);
        
        // then: 전체 제한 횟수가 반환되어야 함
        assertEquals(5, remaining);
        
        // when: 2번 요청 후
        for (int i = 0; i < 2; i++) {
            rateLimitService.checkLimit(CLIENT_IP, rateLimit);
            rateLimitService.increment(CLIENT_IP, rateLimit);
        }
        
        // then: 남은 요청 수는 3이어야 함
        remaining = rateLimitService.getRemaining(CLIENT_IP, rateLimit);
        assertEquals(3, remaining);
    }
    
    @Test
    void 남은_시간_확인_테스트() {
        // given: 5초 윈도우
        RateLimit rateLimit = new RateLimit(10, Duration.ofSeconds(5), ENDPOINT);
        
        // when: 남은 시간 확인
        long remainingTime = rateLimitService.getRemainingTime(CLIENT_IP, rateLimit);
        
        // then: 0초 초과 5초 이내여야 함
        assertTrue(remainingTime > 0 && remainingTime <= 5, 
            "남은 시간은 0초 초과 5초 이하여야 합니다: " + remainingTime);
    }
}
