package io.throttle.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Redis 기반 분산 환경에서의 Throttle 라이브러리 사용 예제
 * 
 * 이 예제는 Redis를 사용하여 여러 인스턴스 간에 Rate Limit과 Quota를 공유하는 방법을 보여줍니다.
 * 
 * @author yogoloper
 * @since 1.0.0
 */
@SpringBootApplication
public class RedisExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisExampleApplication.class, args);
        System.out.println("🚀 Redis Example Application Started!");
        System.out.println("📖 This example demonstrates distributed rate limiting with Redis");
        System.out.println("🔗 Try: http://localhost:8081/api/distributed");
    }
} 