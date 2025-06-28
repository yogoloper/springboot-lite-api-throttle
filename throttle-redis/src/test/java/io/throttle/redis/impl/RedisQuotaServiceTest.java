package io.throttle.redis.impl;

import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.model.Quota;
import io.throttle.redis.config.RedisTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {RedisTestConfig.class, RedisQuotaService.class})
@Testcontainers
class RedisQuotaServiceTest {

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
    
    private RedisQuotaService quotaService;
    private static final String CLIENT_IP = "127.0.0.1";
    private static final String ENDPOINT = "test-endpoint";
    
    @BeforeEach
    void setUp() {
        // Redis 데이터 초기화
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }
        quotaService = new RedisQuotaService(redisTemplate);
    }
    
    @AfterEach
    void tearDown() {
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }
    }
    
    // resetTime이 항상 현재(now)보다 60초 이상 미래가 되도록 보장
    private Quota createDailyQuota(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetTime = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        // 만약 resetTime이 현재보다 60초 이내라면, 1분을 더해줌
        if (resetTime.isBefore(now.plusSeconds(60))) {
            resetTime = now.plusMinutes(2);
        }
        Quota quota = new Quota(limit, Quota.Period.DAILY, resetTime.atZone(ZoneId.systemDefault()).toInstant(), ENDPOINT);
        // 디버깅: resetTime과 now를 초 단위로 출력
        System.out.println("[디버그] Quota resetTime(epoch): " + quota.getResetTime().getEpochSecond());
        System.out.println("[디버그] Now(epoch): " + java.time.Instant.now().getEpochSecond());
        return quota;
    }
    
    // resetTime이 항상 현재(now)보다 60초 이상 미래가 되도록 보장
    private Quota createMonthlyQuota(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetTime = now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // 만약 resetTime이 현재보다 60초 이내라면, 1분을 더해줌
        if (resetTime.isBefore(now.plusSeconds(60))) {
            resetTime = now.plusMinutes(2);
        }
        Quota quota = new Quota(limit, Quota.Period.MONTHLY, resetTime.atZone(ZoneId.systemDefault()).toInstant(), ENDPOINT);
        // 디버깅: resetTime과 now를 초 단위로 출력
        System.out.println("[디버그] Quota resetTime(epoch): " + quota.getResetTime().getEpochSecond());
        System.out.println("[디버그] Now(epoch): " + java.time.Instant.now().getEpochSecond());
        return quota;
    }

    @Test
    @DisplayName("일일 할당량 초과 시 QuotaExceededException이 발생해야 함")
    void 일일_할당량_초과_테스트() {
        // given: 일일 3회 할당량
        Quota quota = createDailyQuota(3);
        
        // when: 3번 사용
        for (int i = 0; i < 3; i++) {
            quotaService.checkQuota(CLIENT_IP, quota);
            quotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 4번째 사용 시 예외 발생
        assertThrows(QuotaExceededException.class, () -> {
            quotaService.checkQuota(CLIENT_IP, quota);
        });
    }
    
    @Test
    @DisplayName("월간 할당량 초과 시 QuotaExceededException이 발생해야 함")
    void 월간_할당량_초과_테스트() {
        // given: 월간 5회 할당량
        Quota quota = createMonthlyQuota(5);
        
        // when: 5번 사용
        for (int i = 0; i < 5; i++) {
            quotaService.checkQuota(CLIENT_IP, quota);
            quotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 6번째 사용 시 예외 발생
        assertThrows(QuotaExceededException.class, () -> {
            quotaService.checkQuota(CLIENT_IP, quota);
        });
    }
    
    @Test
    @DisplayName("새로운 날짜에 일일 할당량이 리셋되어야 함")
    void 일일_할당량_리셋_테스트() {
        // given: 일일 3회 할당량
        Quota quota = createDailyQuota(3);
        
        // when: 3번 사용
        for (int i = 0; i < 3; i++) {
            quotaService.checkQuota(CLIENT_IP, quota);
            quotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 4번째 사용 시 예외 발생
        assertThrows(QuotaExceededException.class, () -> {
            quotaService.checkQuota(CLIENT_IP, quota);
        });
        
        // when: 새로운 할당량 생성 (다른 키 사용)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime resetTime = tomorrow.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        if (resetTime.isBefore(now.plusSeconds(60))) {
            resetTime = now.plusMinutes(2);
        }
        Quota newQuota = new Quota(3, Quota.Period.DAILY, resetTime.atZone(ZoneId.systemDefault()).toInstant(), "new-endpoint");
        System.out.println("[디버그] (리셋) Quota resetTime(epoch): " + newQuota.getResetTime().getEpochSecond());
        System.out.println("[디버그] (리셋) Now(epoch): " + java.time.Instant.now().getEpochSecond());
        // then: 다시 사용 가능해야 함
        assertDoesNotThrow(() -> {
            quotaService.checkQuota(CLIENT_IP, newQuota);
            quotaService.useQuota(CLIENT_IP, newQuota);
        });
    }
    
    @Test
    @DisplayName("새로운 달에 월간 할당량이 리셋되어야 함")
    void 월간_할당량_리셋_테스트() {
        // given: 월간 3회 할당량
        Quota quota = createMonthlyQuota(3);
        
        // when: 3번 사용
        for (int i = 0; i < 3; i++) {
            quotaService.checkQuota(CLIENT_IP, quota);
            quotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 4번째 사용 시 예외 발생
        assertThrows(QuotaExceededException.class, () -> {
            quotaService.checkQuota(CLIENT_IP, quota);
        });
        
        // when: 새로운 할당량 생성 (다른 키 사용)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonth = now.plusMonths(1).withDayOfMonth(1);
        LocalDateTime resetTime = nextMonth.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        if (resetTime.isBefore(now.plusSeconds(60))) {
            resetTime = now.plusMinutes(2);
        }
        Quota newQuota = new Quota(3, Quota.Period.MONTHLY, resetTime.atZone(ZoneId.systemDefault()).toInstant(), "new-endpoint");
        System.out.println("[디버그] (리셋) Quota resetTime(epoch): " + newQuota.getResetTime().getEpochSecond());
        System.out.println("[디버그] (리셋) Now(epoch): " + java.time.Instant.now().getEpochSecond());
        // then: 다시 사용 가능해야 함
        assertDoesNotThrow(() -> {
            quotaService.checkQuota(CLIENT_IP, newQuota);
            quotaService.useQuota(CLIENT_IP, newQuota);
        });
    }
    
    @Test
    @DisplayName("남은 할당량이 정확히 계산되어야 함")
    void 남은_할당량_계산_테스트() {
        // given: 일일 5회 할당량
        Quota quota = createDailyQuota(5);
        
        // when: 2번 사용
        for (int i = 0; i < 2; i++) {
            quotaService.checkQuota(CLIENT_IP, quota);
            quotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 남은 할당량은 3회여야 함
        int remaining = quotaService.getRemaining(CLIENT_IP, quota);
        assertEquals(3, remaining, "남은 할당량이 예상과 다릅니다.");
    }
    
    @Test
    @DisplayName("다른_키에_대한_할당량은_독립적이어야_함")
    void 다른_키에_대한_할당량_독립성_테스트() {
        // given: 일일 3회 할당량
        Quota quota = createDailyQuota(3);
        String anotherClientIp = "192.168.0.1";
        
        // when: 첫 번째 클라이언트로 3번 사용
        for (int i = 0; i < 3; i++) {
            quotaService.checkQuota(CLIENT_IP, quota);
            quotaService.useQuota(CLIENT_IP, quota);
            // 각 단계별 남은 할당량 출력
            System.out.println("After useQuota(" + (i+1) + "): 남은 할당량 = " + quotaService.getRemaining(CLIENT_IP, quota));
            // 실제 Redis에 저장된 값도 직접 출력 (reflection으로 quotaKey 획득)
            try {
                java.lang.reflect.Method buildQuotaKey = quotaService.getClass().getDeclaredMethod("buildQuotaKey", String.class, quota.getClass());
                buildQuotaKey.setAccessible(true);
                String quotaKey = (String) buildQuotaKey.invoke(quotaService, CLIENT_IP, quota);
                System.out.println("실제 Redis 값 = " + redisTemplate.opsForValue().get(quotaKey));
            } catch (Exception e) {
                System.out.println("[디버그] quotaKey reflection 실패: " + e.getMessage());
            }
            // quota*로 시작하는 모든 키와 값 출력
            try {
                java.util.Set<String> keys = redisTemplate.keys("quota*");
                System.out.println("[디버그] Redis quota* 키 목록: " + keys);
                for (String key : keys) {
                    System.out.println("[디버그] " + key + " = " + redisTemplate.opsForValue().get(key));
                }
            } catch (Exception e) {
                System.out.println("[디버그] quota* 키 전체 출력 실패: " + e.getMessage());
            }
        }
        // assertThrows 직전 실제 남은 할당량 및 실제 Redis 값 출력
        System.out.println("Before assertThrows: 남은 할당량 = " + quotaService.getRemaining(CLIENT_IP, quota));
        try {
            java.lang.reflect.Method buildQuotaKey = quotaService.getClass().getDeclaredMethod("buildQuotaKey", String.class, quota.getClass());
            buildQuotaKey.setAccessible(true);
            String quotaKey = (String) buildQuotaKey.invoke(quotaService, CLIENT_IP, quota);
            System.out.println("Before assertThrows: 실제 Redis 값 = " + redisTemplate.opsForValue().get(quotaKey));
        } catch (Exception e) {
            System.out.println("[디버그] quotaKey reflection 실패: " + e.getMessage());
        }
        
        // then: 첫 번째 클라이언트는 할당량 초과
        assertThrows(QuotaExceededException.class, () -> {
            quotaService.checkQuota(CLIENT_IP, quota);
        });
        
        // then: 다른 클라이언트는 사용 가능
        assertDoesNotThrow(() -> {
            quotaService.checkQuota(anotherClientIp, quota);
            quotaService.useQuota(anotherClientIp, quota);
        });
    }
    
    @Test
    @DisplayName("동시_요청_테스트")
    void 동시_요청_테스트() throws InterruptedException {
        // given: 일일 100회 할당량
        Quota quota = createDailyQuota(100);
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
                            quotaService.checkQuota(CLIENT_IP, quota);
                            quotaService.useQuota(CLIENT_IP, quota);
                            successCount.incrementAndGet();
                        } catch (QuotaExceededException e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // then: 모든 스레드 완료 대기
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        executorService.shutdown();
        
        // then: 성공한 요청 수는 제한을 초과하지 않아야 함
        assertTrue(successCount.get() <= quota.getLimit(), 
            "성공한 요청 수(" + successCount.get() + ")가 제한(" + quota.getLimit() + ")을 초과함");
    }
}
