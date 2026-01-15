package com.musinsa.pointsystem.infra.event;

import com.musinsa.pointsystem.domain.event.PointEarnCanceledEvent;
import com.musinsa.pointsystem.domain.event.PointEarnedEvent;
import com.musinsa.pointsystem.domain.event.PointUseCanceledEvent;
import com.musinsa.pointsystem.domain.event.PointUsedEvent;
import com.musinsa.pointsystem.domain.repository.BalanceCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 포인트 도메인 이벤트 핸들러
 * - 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
 * - 캐시 무효화: 트랜잭션 성공 후에만 캐시 무효화 (데이터 정합성 보장)
 * - 추후 용도에 맞게 구현 필요:
 *   - 알림 발송 (푸시, SMS, 이메일 등)
 *   - 통계/분석 데이터 적재
 *   - 외부 시스템 연동 (Kafka 발행 등)
 *   - 감사 로그 기록
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointEventHandler {

    private final BalanceCachePort balanceCachePort;

    /**
     * 포인트 적립 이벤트 핸들러
     * - 추후 용도에 맞게 구현
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointEarned(PointEarnedEvent event) {
        log.info("포인트 적립 이벤트 수신. memberId={}, ledgerId={}, amount={}, earnType={}",
                event.memberId(), event.ledgerId(), event.amount(), event.earnType());

        // 트랜잭션 커밋 후 캐시 무효화 (데이터 정합성 보장)
        balanceCachePort.evictBalanceCache(event.memberId());

        // TODO: 추후 용도에 맞게 구현
        // - 적립 완료 알림 발송
        // - 적립 통계 데이터 적재
        // - 외부 시스템 연동
    }

    /**
     * 포인트 적립취소 이벤트 핸들러
     * - 추후 용도에 맞게 구현
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointEarnCanceled(PointEarnCanceledEvent event) {
        log.info("포인트 적립취소 이벤트 수신. memberId={}, ledgerId={}, amount={}",
                event.memberId(), event.ledgerId(), event.amount());

        // 트랜잭션 커밋 후 캐시 무효화 (데이터 정합성 보장)
        balanceCachePort.evictBalanceCache(event.memberId());

        // TODO: 추후 용도에 맞게 구현
        // - 적립취소 완료 알림 발송
        // - 취소 통계 데이터 적재
        // - 외부 시스템 연동
    }

    /**
     * 포인트 사용 이벤트 핸들러
     * - 추후 용도에 맞게 구현
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointUsed(PointUsedEvent event) {
        log.info("포인트 사용 이벤트 수신. memberId={}, amount={}, orderId={}, usedLedgerCount={}",
                event.memberId(), event.amount(), event.orderId(), event.usedLedgerCount());

        // 트랜잭션 커밋 후 캐시 무효화 (데이터 정합성 보장)
        balanceCachePort.evictBalanceCache(event.memberId());

        // TODO: 추후 용도에 맞게 구현
        // - 사용 완료 알림 발송
        // - 사용 통계 데이터 적재
        // - 외부 시스템 연동
    }

    /**
     * 포인트 사용취소 이벤트 핸들러
     * - 추후 용도에 맞게 구현
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointUseCanceled(PointUseCanceledEvent event) {
        log.info("포인트 사용취소 이벤트 수신. memberId={}, amount={}, orderId={}, newLedgerCount={}",
                event.memberId(), event.amount(), event.orderId(), event.newLedgerCount());

        // 트랜잭션 커밋 후 캐시 무효화 (데이터 정합성 보장)
        balanceCachePort.evictBalanceCache(event.memberId());

        // TODO: 추후 용도에 맞게 구현
        // - 사용취소 완료 알림 발송
        // - 취소 통계 데이터 적재
        // - 외부 시스템 연동
    }
}
