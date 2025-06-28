plugins {
    `java-library`
    `maven-publish`
}

group = "io.throttle"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // 테스트 의존성
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.throttle"
            artifactId = "throttle-core"
            version = "0.0.1-SNAPSHOT"
            
            pom {
                name.set("Throttle Core")
                description.set("Core library for rate limiting and API quota management")
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