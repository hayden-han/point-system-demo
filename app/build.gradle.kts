plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":infra"))

    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Prometheus Metrics
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // SpringDoc OpenAPI (Swagger UI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")

    // Embedded Redis (local 프로파일에서 사용)
    implementation("com.github.codemonstur:embedded-redis:1.4.3")

    // =====================================================
    // test 의존성 - domain, infra 모듈의 testFixtures 재사용
    // =====================================================
    testImplementation(testFixtures(project(":domain")))
    testImplementation(testFixtures(project(":infra")))
}

tasks.bootJar {
    archiveFileName.set("point-app.jar")
}
