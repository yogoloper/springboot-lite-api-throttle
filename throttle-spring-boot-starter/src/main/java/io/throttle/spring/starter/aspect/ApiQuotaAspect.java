package io.throttle.spring.starter.aspect;

import io.throttle.core.api.QuotaService;
import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.model.Quota;
import io.throttle.spring.annotation.ApiQuota;
import io.throttle.spring.starter.RequestKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * ApiQuota 애노테이션을 처리하는 AOP Aspect
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ApiQuotaAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiQuotaAspect.class);

    private final QuotaService quotaService;
    private final RequestKeyGenerator requestKeyGenerator;

    @Around("@annotation(apiQuota)")
    public Object around(ProceedingJoinPoint joinPoint, ApiQuota apiQuota) throws Throwable {
        log.info("=== ApiQuotaAspect triggered ===");
        log.info("Method: {}", joinPoint.getSignature().getName());
        
        String key = requestKeyGenerator.generateKey();
        
        log.info("API quota check for key: {}, daily: {}, monthly: {}", 
                 key, apiQuota.daily(), apiQuota.monthly());
        
        try {
            // 일일 Quota 체크 및 사용
            Instant dailyResetTime = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Quota dailyQuota = new Quota(
                apiQuota.daily(), 
                Quota.Period.DAILY, 
                dailyResetTime, 
                key + ":daily"
            );
            log.info("Checking daily quota: {}", dailyQuota);
            quotaService.checkQuota(key + ":daily", dailyQuota);
            quotaService.useQuota(key + ":daily", dailyQuota);
            log.info("Daily quota check passed");
            
            // 월간 Quota 체크 및 사용
            LocalDateTime nextMonth = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            Instant monthlyResetTime = nextMonth.atZone(ZoneId.systemDefault()).toInstant();
            Quota monthlyQuota = new Quota(
                apiQuota.monthly(), 
                Quota.Period.MONTHLY, 
                monthlyResetTime, 
                key + ":monthly"
            );
            log.info("Checking monthly quota: {}", monthlyQuota);
            quotaService.checkQuota(key + ":monthly", monthlyQuota);
            quotaService.useQuota(key + ":monthly", monthlyQuota);
            log.info("Monthly quota check passed");
            
            // 요청 처리
            Object result = joinPoint.proceed();
            log.info("API quota check completed successfully");
            
            return result;
            
        } catch (QuotaExceededException e) {
            log.info("API quota exceeded for key: {}", key);
            throw e;
        } catch (Exception e) {
            log.error("Error in ApiQuotaAspect: {}", e.getMessage(), e);
            throw e;
        }
    }
} 