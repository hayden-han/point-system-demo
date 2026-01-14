package com.musinsa.pointsystem;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CoreTestApplication.class)
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
public abstract class IntegrationTestBase {
}
