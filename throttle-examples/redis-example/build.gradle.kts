plugins {
    id("java")
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "io.throttle.examples"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Throttle Library
    implementation(project(":throttle-spring-boot-starter"))
    implementation(project(":throttle-redis"))
    
    // Redis 클라이언트
    implementation("io.lettuce:lettuce-core")
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.19.7")
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
} 