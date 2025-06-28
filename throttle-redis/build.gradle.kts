plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.4"
    `maven-publish`
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
    
    // Spring Data Redis
    api("org.springframework.data:spring-data-redis:3.2.6")
    implementation("io.lettuce:lettuce-core:6.2.3.RELEASE")
    
    // Spring Context (Optional, 필요시만)
    implementation("org.springframework:spring-context:6.1.6")
    
    // Lombok (보일러플레이트 코드 감소, 필요시만)
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    
    // 테스트 의존성
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.6") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.throttle"
            artifactId = "throttle-redis"
            version = "0.0.1-SNAPSHOT"
            
            pom {
                name.set("Throttle Redis")
                description.set("Redis implementation for distributed rate limiting and API quota management")
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