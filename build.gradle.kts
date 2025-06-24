// build.gradle.kts
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

group = "io.github.yogoloper"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter
    api("org.springframework.boot:spring-boot-starter")
    
    // Spring Web (API 엔드포인트를 위한 의존성)
    api("org.springframework.boot:spring-boot-starter-web")
    
    // Spring Data Redis (분산 환경 지원을 위해)
    api("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Guava (In-memory 캐시를 위해)
    api("com.google.guava:guava:32.1.3-jre")
    
    // JWT (사용자 인증을 위해)
    api("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Lombok (보일러플레이트 코드 감소)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.github.yogoloper"
            artifactId = "springboot-lite-api-throttle"
            version = "0.0.1-SNAPSHOT"
            
            pom {
                name.set("Spring Boot Lite API Throttle")
                description.set("A lightweight rate limiting and API quota management library for Spring Boot applications")
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
                
                scm {
                    connection.set("scm:git:git://github.com/yogoloper/springboot-lite-api-throttle.git")
                    developerConnection.set("scm:git:ssh://github.com:yogoloper/springboot-lite-api-throttle.git")
                    url.set("https://github.com/yogoloper/springboot-lite-api-throttle")
                }
            }
        }
    }
}