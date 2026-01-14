plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":infra"))

    // Spring Batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // Spring Batch Test
    testImplementation("org.springframework.batch:spring-batch-test")

    // =====================================================
    // test 의존성 - domain, infra 모듈의 testFixtures 재사용
    // =====================================================
    testImplementation(testFixtures(project(":domain")))
    testImplementation(testFixtures(project(":infra")))
}

tasks.bootJar {
    archiveFileName.set("batch.jar")
}
