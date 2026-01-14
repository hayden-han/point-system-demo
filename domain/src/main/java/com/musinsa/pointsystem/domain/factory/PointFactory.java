package com.musinsa.pointsystem.domain.factory;

import com.musinsa.pointsystem.domain.port.TimeProvider;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.port.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 포인트 도메인 객체 생성 팩토리
 * - ID 생성 로직을 캡슐화
 * - 도메인 모델의 순수성 유지
 * - 프레임워크 독립적 (Spring 어노테이션 없음)
 */
public class PointFactory {

    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public PointFactory(IdGenerator idGenerator, TimeProvider timeProvider) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    // === PointLedger 생성 ===

    /**
     * 포인트 적립건 생성 (EARN Entry 포함)
     */
    public PointLedger createLedger(UUID memberId, PointAmount amount, EarnType earnType, LocalDateTime expiredAt) {
        LocalDateTime now = timeProvider.now();
        return PointLedger.create(
                idGenerator.generate(),
                memberId,
                amount,
                earnType,
                expiredAt,
                null,
                idGenerator,
                now
        );
    }

    /**
     * 사용 취소로 인한 포인트 적립건 생성 (만료된 적립건 복원용)
     */
    public PointLedger createLedgerFromCancelUse(UUID memberId, PointAmount amount, EarnType earnType,
                                                  LocalDateTime expiredAt, UUID sourceLedgerId) {
        LocalDateTime now = timeProvider.now();
        return PointLedger.create(
                idGenerator.generate(),
                memberId,
                amount,
                earnType,
                expiredAt,
                sourceLedgerId,
                idGenerator,
                now
        );
    }

    // === 헬퍼 메서드 ===

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }
}
