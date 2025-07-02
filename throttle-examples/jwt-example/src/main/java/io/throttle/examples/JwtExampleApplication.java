package io.throttle.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JWT 토큰 기반 사용자별 API 제한 예제
 * 
 * 이 예제는 JWT 토큰을 사용하여 사용자별로 다른 Rate Limit과 Quota를
 * 적용하는 방법을 보여줍니다.
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@SpringBootApplication
public class JwtExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtExampleApplication.class, args);
        System.out.println("🚀 JWT Example Application Started!");
        System.out.println("📖 This example demonstrates user-based rate limiting with JWT");
        System.out.println("🔗 Try: http://localhost:8082/api/user-profile");
        System.out.println("🔑 Login: http://localhost:8082/api/auth/login");
    }
} 