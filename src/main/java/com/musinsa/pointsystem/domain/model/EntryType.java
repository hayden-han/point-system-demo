package com.musinsa.pointsystem.domain.model;

/**
 * 적립건 변동 유형
 */
public enum EntryType {
    /** 적립 (+) */
    EARN,
    /** 적립취소 (-) */
    EARN_CANCEL,
    /** 사용 (-) */
    USE,
    /** 사용취소 (+) */
    USE_CANCEL
}
