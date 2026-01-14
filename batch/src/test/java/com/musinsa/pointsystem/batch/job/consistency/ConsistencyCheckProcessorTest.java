package com.musinsa.pointsystem.batch.job.consistency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsistencyCheckProcessor 테스트")
class ConsistencyCheckProcessorTest {

    private ConsistencyCheckProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ConsistencyCheckProcessor();
    }

    @Nested
    @DisplayName("정합성 일치 케이스")
    class ConsistentCases {

        @Test
        @DisplayName("모든 금액이 Entry 계산값과 일치하면 consistent 반환")
        void shouldReturnConsistentWhenAllAmountsMatch() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,  // earnedAmount
                    800L,   // availableAmount
                    200L,   // usedAmount
                    false,  // isCanceled
                    1000L,  // entryEarnSum
                    0L,     // entryEarnCancelSum
                    -200L,  // entryUseSum (음수)
                    0L      // entryUseCancelSum
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isTrue();
            assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.NONE);
            assertThat(result.details()).isNull();
        }

        @Test
        @DisplayName("취소된 Ledger는 available이 0이면 consistent")
        void shouldReturnConsistentWhenCanceledLedgerHasZeroAvailable() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,  // earnedAmount (취소되어도 원래 값 유지)
                    0L,     // availableAmount (취소 시 0)
                    0L,     // usedAmount
                    true,   // isCanceled
                    1000L,  // entryEarnSum
                    0L,     // entryEarnCancelSum
                    0L,     // entryUseSum
                    0L      // entryUseCancelSum
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isTrue();
        }

        @Test
        @DisplayName("USE_CANCEL로 환불된 경우 정합성 검증")
        void shouldHandleUseCancelCorrectly() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            // 1000원 적립 -> 500원 사용 -> 500원 환불
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,  // earnedAmount
                    1000L,  // availableAmount (사용 후 환불되어 원복)
                    0L,     // usedAmount (사용 취소되어 0)
                    false,
                    1000L,  // entryEarnSum
                    0L,     // entryEarnCancelSum
                    -500L,  // entryUseSum (사용)
                    500L    // entryUseCancelSum (환불)
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isTrue();
        }
    }

    @Nested
    @DisplayName("정합성 불일치 케이스")
    class InconsistentCases {

        @Test
        @DisplayName("availableAmount 불일치 시 AVAILABLE_AMOUNT_MISMATCH 반환")
        void shouldReturnAvailableAmountMismatch() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,  // earnedAmount
                    900L,   // availableAmount (잘못된 값, 실제는 800이어야 함)
                    200L,   // usedAmount
                    false,
                    1000L,  // entryEarnSum
                    0L,
                    -200L,  // entryUseSum
                    0L
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isFalse();
            assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.AVAILABLE_AMOUNT_MISMATCH);
            assertThat(result.details()).contains("availableAmount");
        }

        @Test
        @DisplayName("usedAmount 불일치 시 USED_AMOUNT_MISMATCH 반환")
        void shouldReturnUsedAmountMismatch() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,
                    800L,   // availableAmount (정상)
                    300L,   // usedAmount (잘못된 값, 실제는 200이어야 함)
                    false,
                    1000L,
                    0L,
                    -200L,
                    0L
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isFalse();
            assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.USED_AMOUNT_MISMATCH);
            assertThat(result.details()).contains("usedAmount");
        }

        @Test
        @DisplayName("earnedAmount 불일치 시 EARNED_AMOUNT_MISMATCH 반환")
        void shouldReturnEarnedAmountMismatch() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1500L,  // earnedAmount (잘못된 값, 실제는 1000이어야 함)
                    1000L,
                    0L,
                    false,
                    1000L,  // entryEarnSum
                    0L,
                    0L,
                    0L
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isFalse();
            assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.EARNED_AMOUNT_MISMATCH);
            assertThat(result.details()).contains("earnedAmount");
        }

        @Test
        @DisplayName("여러 필드 불일치 시 MULTIPLE_MISMATCHES 반환")
        void shouldReturnMultipleMismatches() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,
                    999L,   // availableAmount 불일치
                    999L,   // usedAmount 불일치
                    false,
                    1000L,
                    0L,
                    -200L,
                    0L
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isFalse();
            assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.MULTIPLE_MISMATCHES);
        }

        @Test
        @DisplayName("취소된 Ledger의 earnedAmount는 검증하지 않음")
        void shouldNotValidateEarnedAmountForCanceledLedger() {
            // given
            UUID ledgerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    ledgerId, memberId,
                    1000L,  // earnedAmount (취소되어도 원래 값 유지)
                    0L,     // availableAmount (취소 시 0)
                    0L,
                    true,   // isCanceled
                    500L,   // entryEarnSum (불일치하지만 취소 상태라 무시)
                    0L,
                    0L,
                    0L
            );

            // when
            ConsistencyCheckResult result = processor.process(dto);

            // then
            assertThat(result.isConsistent()).isTrue();
        }
    }

    @Nested
    @DisplayName("LedgerConsistencyDto 계산 로직 테스트")
    class DtoCalculationTest {

        @Test
        @DisplayName("calculatedAvailableAmount: 적립 - 사용")
        void shouldCalculateAvailableAmount() {
            // given
            LedgerConsistencyDto dto = createDto(
                    UUID.randomUUID(), UUID.randomUUID(),
                    0L, 0L, 0L, false,
                    1000L,  // entryEarnSum
                    0L,     // entryEarnCancelSum
                    -300L,  // entryUseSum (음수)
                    0L      // entryUseCancelSum
            );

            // when & then
            assertThat(dto.calculatedAvailableAmount()).isEqualTo(700L);
        }

        @Test
        @DisplayName("calculatedUsedAmount: |USE| - USE_CANCEL")
        void shouldCalculateUsedAmount() {
            // given
            LedgerConsistencyDto dto = createDto(
                    UUID.randomUUID(), UUID.randomUUID(),
                    0L, 0L, 0L, false,
                    1000L,
                    0L,
                    -500L,  // entryUseSum (음수)
                    200L    // entryUseCancelSum (양수)
            );

            // when & then
            assertThat(dto.calculatedUsedAmount()).isEqualTo(300L);  // 500 - 200
        }

        @Test
        @DisplayName("취소된 Ledger의 calculatedAvailableAmount는 0")
        void shouldReturnZeroAvailableForCanceledLedger() {
            // given
            LedgerConsistencyDto dto = createDto(
                    UUID.randomUUID(), UUID.randomUUID(),
                    0L, 0L, 0L, true,  // isCanceled
                    1000L, 0L, 0L, 0L
            );

            // when & then
            assertThat(dto.calculatedAvailableAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("UUID 변환 테스트")
        void shouldConvertBytesToUuid() {
            // given
            UUID originalUuid = UUID.randomUUID();
            LedgerConsistencyDto dto = createDto(
                    originalUuid, UUID.randomUUID(),
                    0L, 0L, 0L, false,
                    0L, 0L, 0L, 0L
            );

            // when & then
            assertThat(dto.ledgerId()).isEqualTo(originalUuid);
        }
    }

    private LedgerConsistencyDto createDto(
            UUID ledgerId, UUID memberId,
            long earnedAmount, long availableAmount, long usedAmount, boolean isCanceled,
            long entryEarnSum, long entryEarnCancelSum, long entryUseSum, long entryUseCancelSum
    ) {
        return new LedgerConsistencyDto(
                uuidToBytes(ledgerId),
                uuidToBytes(memberId),
                earnedAmount,
                availableAmount,
                usedAmount,
                isCanceled,
                entryEarnSum,
                entryEarnCancelSum,
                entryUseSum,
                entryUseCancelSum
        );
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
