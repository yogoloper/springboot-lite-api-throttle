plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    `java`
}

group = "io.throttle"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    mavenLocal() // 로컬 라이브러리 사용
}

dependencies {
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Throttle 라이브러리 (로컬)
    implementation(project(":throttle-spring-boot-starter"))
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// 소스셋 명시적 설정
sourceSets {
    main {
        java.srcDirs("src/main/java")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/java")
        resources.srcDirs("src/test/resources")
    }
}

// Spring Boot 설정
springBoot {
    mainClass.set("io.throttle.examples.BasicUsageApplication")
}

// 중복 파일 처리 전략 설정
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 프로젝트: throttle-examples:basic-example 