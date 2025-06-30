plugins {
    `java-library`
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    `maven-publish`
}

// 부트 애플리케이션이 아닌 라이브러리로 빌드하도록 설정
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
    archiveClassifier.set("") // 플레인 JAR로 생성
}

group = "io.throttle"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // Core 모듈 의존성
    api(project(":throttle-core"))
    
    // Memory 모듈 의존성
    api(project(":throttle-memory"))
    
    // Redis 모듈 의존성
    api(project(":throttle-redis"))
    
    // Spring Boot Starter (핵심 의존성)
    api("org.springframework.boot:spring-boot-starter")
    
    // Spring Web (RequestKeyGenerator를 위해)
    api("org.springframework.boot:spring-boot-starter-web")
    
    // Spring AOP (Aspect를 위해)
    api("org.springframework.boot:spring-boot-starter-aop")
    
    // Spring Boot Auto Configuration
    api("org.springframework.boot:spring-boot-autoconfigure")
    
    // Spring Boot Configuration Processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // JWT (사용자 인증을 위해) - 선택적 의존성
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Lombok (보일러플레이트 코드 감소)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.throttle"
            artifactId = "throttle-spring-boot-starter"
            version = "0.0.1-SNAPSHOT"
            
            pom {
                name.set("Throttle Spring Boot Starter")
                description.set("Spring Boot Starter for rate limiting and API quota management")
                url.set("https://github.com/yogoloper/springboot-lite-api-throttle")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("yogoloper")
                        name.set("Yogoloper")
                        email.set("your.email@example.com")
                    }
                }
            }
        }
    }
} 