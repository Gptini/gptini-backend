buildscript {
    dependencies {
        classpath("com.mysql:mysql-connector-j:9.1.0")
        classpath("org.flywaydb:flyway-mysql:11.2.0")
    }
}

plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.2.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

group = "com.gptini"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")

    // Flyway
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-mysql")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // AWS S3 (파일 업로드용)
    implementation("software.amazon.awssdk:s3:2.29.51")

    // Swagger / OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // Dev & Test
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // H2 for local development
    runtimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Flyway Gradle Task 설정
flyway {
    url = env.fetchOrNull("DB_URL")
    user = env.fetchOrNull("DB_USERNAME")
    password = env.fetchOrNull("DB_PASSWORD")
    locations = arrayOf("classpath:db/migration")
}

// flywayMigrate 실행 전 클래스 빌드 필요
tasks.named("flywayMigrate") {
    dependsOn("processResources")
}
