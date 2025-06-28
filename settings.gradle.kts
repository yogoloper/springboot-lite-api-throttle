rootProject.name = "springboot-lite-api-throttle"

// 핵심 모듈들
include("throttle-core")
include("throttle-spring-boot-starter")

// 저장소별 모듈들
include("throttle-memory")
include("throttle-redis")

// 예제 모듈들
include("throttle-examples:basic-example")
include("throttle-examples:redis-example")
