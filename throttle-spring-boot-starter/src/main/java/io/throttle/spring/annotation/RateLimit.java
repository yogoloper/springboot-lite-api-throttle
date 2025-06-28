package io.throttle.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting을 적용하기 위한 애노테이션
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 허용되는 최대 요청 수
     */
    int requests() default 100;
    
    /**
     * 시간 단위
     */
    TimeUnit per() default TimeUnit.MINUTES;
    
    /**
     * 요청 식별 키 타입
     * - "ip": IP 주소 기반
     * - "user": JWT 사용자 기반
     * - "header:HeaderName": 특정 헤더 값 기반
     * - "param:ParamName": 특정 파라미터 값 기반
     */
    String key() default "ip";
    
    /**
     * 에러 메시지 (선택사항)
     */
    String message() default "";
    
    /**
     * Rate Limit을 건너뛸 조건 (SpEL 표현식)
     * 예: "#{@userService.isPremium(#request)}"
     */
    String skipIf() default "";
} 