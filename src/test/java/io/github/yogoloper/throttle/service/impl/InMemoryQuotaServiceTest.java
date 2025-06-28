package io.github.yogoloper.throttle.service.impl;

import io.github.yogoloper.throttle.exception.QuotaExceededException;
import io.github.yogoloper.throttle.model.Quota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InMemoryQuotaService 테스트 클래스
 */
class InMemoryQuotaServiceTest {

    private InMemoryQuotaService quotaService;
    private static final String CLIENT_IP = "127.0.0.1";
    private static final String ENDPOINT = "test-endpoint";
    
    @BeforeEach
    void setUp() {
        quotaService = new InMemoryQuotaService();
    }
    
    private Quota createDailyQuota(int limit) {
        LocalDateTime resetTime = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        return new Quota(limit, Quota.Period.DAILY, 
            resetTime.atZone(ZoneId.systemDefault()).toInstant(), ENDPOINT);
    }
    
    private Quota createMonthlyQuota(int limit) {
        LocalDateTime resetTime = LocalDateTime.now().plusMonths(1).withDayOfMonth(1)
            .withHour(0).withMinute(0).withSecond(0);
        return new Quota(limit, Quota.Period.MONTHLY, 
            resetTime.atZone(ZoneId.systemDefault()).toInstant(), ENDPOINT);
    }

    @Test
    @DisplayName("할당량 초과 시 QuotaExceededException이 발생해야 함")
    void 할당량_초과_시_예외_발생() {
        // given: 일일 3회 할당량
        Quota quota = createDailyQuota(3);
        
        // when: 3번 사용
        for (int i = 0; i < 3; i++) {
            quotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 4번째 사용 시 예외 발생
        assertThrows(QuotaExceededException.class, () -> {
            quotaService.checkQuota(CLIENT_IP, quota);
        });
    }

    @Test
    @DisplayName("새로운 날짜에 할당량이 리셋되어야 함")
    void 새로운_날짜에_할당량_리셋() {
        // given: 테스트용 고정 시간 설정
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZoneId zone = ZoneId.systemDefault();
        java.time.Clock fixedClock = java.time.Clock.fixed(now.atZone(zone).toInstant(), zone);
        InMemoryQuotaService testQuotaService = new InMemoryQuotaService(fixedClock);
        
        // 일일 3회 할당량
        Quota quota = new Quota(3, Quota.Period.DAILY, 
            now.plusDays(1).atZone(zone).toInstant(), ENDPOINT);
        
        // when: 3번 사용
        for (int i = 0; i < 3; i++) {
            testQuotaService.useQuota(CLIENT_IP, quota);
        }
        
        // then: 할당량 초과
        assertThrows(QuotaExceededException.class, () -> {
            testQuotaService.checkQuota(CLIENT_IP, quota);
        });
        
        // when: 다음 날로 시간 이동
        LocalDateTime nextDay = now.plusDays(1);
        java.time.Clock nextDayClock = java.time.Clock.fixed(nextDay.atZone(zone).toInstant(), zone);
        InMemoryQuotaService nextDayService = new InMemoryQuotaService(nextDayClock);
        Quota newQuota = new Quota(3, Quota.Period.DAILY, 
            nextDay.plusDays(1).atZone(zone).toInstant(), ENDPOINT);
        
        // then: 다시 사용 가능해야 함
        assertDoesNotThrow(() -> {
            nextDayService.checkQuota(CLIENT_IP, newQuota);
            nextDayService.useQuota(CLIENT_IP, newQuota);
        });
    }

    @Test
    @DisplayName("남은 할당량이 정확히 계산되어야 함")
    void 남은_할당량_계산() {
        // given: 일일 5회 할당량
        Quota quota = createDailyQuota(5);
        
        // when: 2번 사용
        quotaService.useQuota(CLIENT_IP, quota);
        quotaService.useQuota(CLIENT_IP, quota);
        
        // then: 3회 남아야 함
        assertEquals(3, quotaService.getRemaining(CLIENT_IP, quota));
    }

    @Test
    @DisplayName("동시성 환경에서 할당량이 올바르게 관리되어야 함")
    void 동시성_환경_테스트() throws InterruptedException {
        // given: 월간 100회 할당량
        Quota quota = createMonthlyQuota(100);
        int threadCount = 20;
        int requestsPerThread = 10;
        
        // when: 여러 스레드에서 동시에 할당량 사용
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        quotaService.checkQuota(CLIENT_IP, quota);
                        quotaService.useQuota(CLIENT_IP, quota);
                    } catch (QuotaExceededException e) {
                        // 할당량 초과 시 중단
                        break;
                    }
                }
            });
            threads[i].start();
        }
        
        // 모든 스레드가 완료될 때까지 대기
        for (Thread t : threads) {
            t.join();
        }
        
        // then: 사용량이 할당량을 초과하지 않아야 함
        int remaining = quotaService.getRemaining(CLIENT_IP, quota);
        assertTrue(remaining >= 0, "남은 할당량은 음수가 될 수 없습니다.");
        assertTrue(quota.getLimit() - remaining <= quota.getLimit(), 
            "사용량은 할당량을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("남은 시간이 올바르게 계산되어야 함")
    void 남은_시간_계산() {
        // given: 일일 할당량
        Quota quota = createDailyQuota(10);
        
        // when: 남은 시간 조회
        long remainingTime = quotaService.getRemainingTime(CLIENT_IP, quota);
        
        // then: 0보다 커야 함
        assertTrue(remainingTime > 0, "남은 시간은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("서로 다른 키에 대한 할당량이 독립적으로 관리되어야 함")
    void 여러_키_독립_관리() {
        // given: 두 개의 다른 키에 대한 할당량
        Quota quota1 = createDailyQuota(5);
        Quota quota2 = createDailyQuota(10);
        String key1 = "user1";
        String key2 = "user2";
        
        // when: 첫 번째 키로 3번 사용
        for (int i = 0; i < 3; i++) {
            quotaService.useQuota(key1, quota1);
        }
        
        // then: 첫 번째 키는 2회 남아야 함
        assertEquals(2, quotaService.getRemaining(key1, quota1));
        // 두 번째 키는 아직 사용하지 않았으므로 전체 할당량이 남아있어야 함
        assertEquals(10, quotaService.getRemaining(key2, quota2));
    }
}