plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.autota"
version = "0.0.1-SNAPSHOT"
description = "core-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Web MVC
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql")
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Spring for RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    // Spring Boot Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // @SpringBootTest는 main을 거치지 않으므로 테스트 JVM에도 서버 타임존을 고정한다
    systemProperty("user.timezone", "Asia/Seoul")
}
