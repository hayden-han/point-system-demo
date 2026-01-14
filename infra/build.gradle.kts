plugins {
    id("java-library")
    id("java-test-fixtures")
}

val querydslVersion = "5.1.0"

dependencies {
    // =====================================================
    // Domain 모듈 의존성 (Repository 인터페이스 구현)
    // =====================================================
    api(project(":domain"))

    // =====================================================
    // Core 모듈 의존성 (공통 유틸리티)
    // =====================================================
    api(project(":core"))

    // =====================================================
    // Spring Boot (인프라 구현용)
    // =====================================================
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-aop")

    // Cache
    api("org.springframework.boot:spring-boot-starter-cache")

    // Actuator & Micrometer (메트릭 수집)
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-core")

    // Redis (Redisson for distributed lock)
    api("org.redisson:redisson-spring-boot-starter:3.40.2")

    // UUID v7 Generator
    api("com.fasterxml.uuid:java-uuid-generator:5.1.0")

    // QueryDSL
    api("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Database (runtime)
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // =====================================================
    // testFixtures 의존성 (다른 모듈에서 재사용 가능)
    // =====================================================
    testFixturesApi(project(":domain"))
    testFixturesApi(project(":core"))
    testFixturesApi(testFixtures(project(":domain")))
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("com.github.codemonstur:embedded-redis:1.4.3")

    // =====================================================
    // test 의존성 (infra 모듈 자체 테스트용)
    // =====================================================
    testImplementation(testFixtures(project(":domain")))
    testImplementation(testFixtures(project(":infra")))
}

// QueryDSL 생성 경로 설정
tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(file("${layout.buildDirectory.get()}/generated/sources/annotationProcessor/java/main"))
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/sources/annotationProcessor/java/main")
        }
    }
}
