# springboot-lite-api-throttle

Spring Boot REST API를 위한 **Lightweight Rate Limiting & API Quota 관리 라이브러리**  
👉 IP / User(JWT 기반) 타입 지원  
👉 단일 인스턴스 (In-memory) + 분산 시스템 (Redis) 모두 지원  
👉 표준 JSON 에러 응답 제공  

[![Build Status](https://github.com/[your-username]/springboot-lite-api-throttle/workflows/CI/badge.svg)](https://github.com/[your-username]/springboot-lite-api-throttle/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.example/springboot-lite-api-throttle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.example/springboot-lite-api-throttle)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📌 프로젝트 개요

**springboot-lite-api-throttle** 는 Spring Boot API 서버를 보호하기 위한  
경량화된 Rate Limiting + Quota 관리 라이브러리입니다.

### ✨ 주요 특징

✅ **간편한 어노테이션 기반 설정** - `@RateLimit`, `@ApiQuota`  
✅ **다양한 제한 방식** - IP 기반, JWT User 기반 요청 제한  
✅ **이중 보호 체계** - Rate Limit (짧은 기간 속도 제한) + Quota (일간/월간 누적 요청 제한)  
✅ **유연한 저장소** - Redis 또는 In-Memory (Guava Cache) 지원  
✅ **표준화된 응답** - 일관된 JSON 에러 응답 형식  
✅ **Spring Boot Auto Configuration** - 최소한의 설정으로 즉시 사용 가능  

### 🎯 사용 사례

- **공개 API 보호**: 무료 사용자에게 일일 1000회 제한
- **DDoS 방어**: IP당 초당 10회 요청 제한
- **마이크로서비스**: Redis 기반 분산 환경에서 통합 제한

---

## 🚀 설치 방법

### Maven
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>springboot-lite-api-throttle</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.example:springboot-lite-api-throttle:1.0.0'
```

### 기본 설정

**application.yml**에 다음 설정 추가:

```yaml
# 기본 In-Memory 사용
api-throttle:
  enabled: true
  default-storage: memory

# Redis 사용 시
api-throttle:
  enabled: true
  default-storage: redis
  redis:
    host: localhost
    port: 6379
    database: 0
```

---

## 📖 사용 방법

### 1. 기본 Rate Limiting

```java
@RestController
public class ApiController {
    
    @RateLimit(requests = 10, per = TimeUnit.MINUTES, key = "ip")
    @GetMapping("/api/public")
    public ResponseEntity<?> getPublicData() {
        return ResponseEntity.ok("Public data");
    }
}
```

### 2. JWT 사용자 기준 제한

```java
@RateLimit(requests = 100, per = TimeUnit.HOUR, key = "user")
@ApiQuota(daily = 1000, monthly = 30000)
@GetMapping("/api/user-data")
public ResponseEntity<?> getUserData() {
    return ResponseEntity.ok("User specific data");
}
```

### 3. 커스텀 키 기반 제한

```java
@RateLimit(requests = 5, per = TimeUnit.MINUTES, key = "header:X-API-Key")
@GetMapping("/api/premium")
public ResponseEntity<?> getPremiumData() {
    return ResponseEntity.ok("Premium data");
}
```

---

## 🔧 응답 형식

### Rate Limit 초과 시 (HTTP 429)
```json
{
  "error": "Too Many Requests",
  "type": "rate_limit",
  "limit": 10,
  "remaining": 0,
  "retryAfterSeconds": 30,
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 일간 Quota 초과 시 (HTTP 429)
```json
{
  "error": "Quota Exceeded",
  "type": "quota_limit",
  "limit": 1000,
  "remaining": 0,
  "period": "daily",
  "resetTime": "2024-01-02T00:00:00Z"
}
```

### 월간 Quota 초과 시 (HTTP 429)
```json
{
  "error": "Quota Exceeded",
  "type": "quota_limit",
  "limit": 30000,
  "remaining": 0,
  "period": "monthly",
  "resetTime": "2024-02-01T00:00:00Z"
}
```

---

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### 개발 환경 설정

```bash
git clone https://github.com/yogoloper/springboot-lite-api-throttle.git
cd springboot-lite-api-throttle
./gradlew clean build
```

---

## 📋 요구사항

- Java 11 이상
- Spring Boot 2.7.0 이상
- (선택사항) Redis 6.0 이상

---

## 📄 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE) 하에 배포됩니다.

```
MIT License

Copyright (c) 2024 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 지원

- 🐛 [Issue 리포트](https://github.com/yogoloper/springboot-lite-api-throttle/issues)
- 💬 [Discussions](https://github.com/yogoloper/springboot-lite-api-throttle/discussions)

---

## 🏆 감사의 말

이 프로젝트는 다음 오픈소스 프로젝트들에서 영감을 받았습니다:
- [Bucket4j](https://github.com/bucket4j/bucket4j)
- [Spring Boot Starter](https://spring.io/projects/spring-boot)

⭐ 이 프로젝트가 유용하다면 Star를 눌러주세요!