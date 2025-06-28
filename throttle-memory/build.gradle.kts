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
    
    // Guava (In-memory 캐시를 위해) - 필수 의존성
    api("com.google.guava:guava:32.1.3-jre")
    
    // Spring Context (Optional, 필요시만)
    implementation("org.springframework:spring-context:6.1.6")
    
    // Lombok (보일러플레이트 코드 감소, 필요시만)
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    
    // 테스트 의존성
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
    testImplementation("org.mockito:mockito-junit-jupiter:5.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.throttle"
            artifactId = "throttle-memory"
            version = "0.0.1-SNAPSHOT"
            
            pom {
                name.set("Throttle Memory")
                description.set("In-memory implementation for rate limiting and API quota management")
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