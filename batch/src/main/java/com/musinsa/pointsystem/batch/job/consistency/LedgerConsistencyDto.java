package com.musinsa.pointsystem.batch.job.consistency;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Ledger 정합성 검증용 DTO
 * - DB에서 조회한 Ledger 정보와 Entry 집계 결과
 */
public record LedgerConsistencyDto(
        byte[] id,
        byte[] memberId,
        long earnedAmount,
        long availableAmount,
        long usedAmount,
        boolean isCanceled,
        long entryEarnSum,
        long entryEarnCancelSum,
        long entryUseSum,
        long entryUseCancelSum
) {
    /**
     * Ledger ID를 UUID로 변환
     */
    public UUID ledgerId() {
        return bytesToUuid(id);
    }

    /**
     * Member ID를 UUID로 변환
     */
    public UUID memberUuid() {
        return bytesToUuid(memberId);
    }

    /**
     * Entry 기반 계산 - 적립 총액
     * EARN은 양수, EARN_CANCEL은 음수로 저장됨
     */
    public long calculatedEarnedAmount() {
        return entryEarnSum + entryEarnCancelSum;  // EARN_CANCEL은 이미 음수
    }

    /**
     * Entry 기반 계산 - 사용 총액 (절대값)
     * USE는 음수, USE_CANCEL은 양수로 저장됨
     */
    public long calculatedUsedAmount() {
        return Math.abs(entryUseSum) - entryUseCancelSum;  // USE는 음수이므로 절대값
    }

    /**
     * Entry 기반 계산 - 사용 가능 금액
     * = 적립총액 - 사용총액
     */
    public long calculatedAvailableAmount() {
        if (isCanceled) {
            return 0L;  // 취소된 Ledger는 available이 0이어야 함
        }
        return calculatedEarnedAmount() - calculatedUsedAmount();
    }

    private static UUID bytesToUuid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }
}
