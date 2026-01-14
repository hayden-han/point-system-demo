plugins {
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    // Domain 모듈은 순수 Java - Spring 의존성 최소화
    // Validation만 사용 (Jakarta Bean Validation API)
    api("jakarta.validation:jakarta.validation-api")

    // =====================================================
    // testFixtures 의존성 (Fixture 클래스용)
    // =====================================================
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")

    // =====================================================
    // test 의존성
    // =====================================================
    testImplementation(testFixtures(project(":domain")))
}
