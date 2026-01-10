package com.musinsa.pointsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointPolicy {
    private final Long id;
    private final String policyKey;
    private final Long policyValue;
    private final String description;

    public static final String EARN_MIN_AMOUNT = "EARN_MIN_AMOUNT";
    public static final String EARN_MAX_AMOUNT = "EARN_MAX_AMOUNT";
    public static final String BALANCE_MAX_AMOUNT = "BALANCE_MAX_AMOUNT";
    public static final String EXPIRATION_DEFAULT_DAYS = "EXPIRATION_DEFAULT_DAYS";
    public static final String EXPIRATION_MIN_DAYS = "EXPIRATION_MIN_DAYS";
    public static final String EXPIRATION_MAX_DAYS = "EXPIRATION_MAX_DAYS";
}
