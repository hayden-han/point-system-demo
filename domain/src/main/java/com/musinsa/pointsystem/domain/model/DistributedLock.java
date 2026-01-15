package com.musinsa.pointsystem.domain.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 분산락 어노테이션
 * - Domain 계층에서 정의하여 모든 계층에서 사용 가능
 * - Infra 계층에서 AOP로 구현
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key();
    long waitTime() default 3000;
    long leaseTime() default 5000;
}
