package io.throttle.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API Quota를 적용하기 위한 애노테이션
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiQuota {
    
    /**
     * 일일 허용 요청 수
     */
    int daily() default 1000;
    
    /**
     * 월간 허용 요청 수
     */
    int monthly() default 30000;
    
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
     * Quota를 건너뛸 조건 (SpEL 표현식)
     * 예: "#{@userService.isPremium(#request)}"
     */
    String skipIf() default "";
} 