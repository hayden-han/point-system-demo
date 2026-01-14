package com.musinsa.pointsystem.batch.job.consistency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LedgerConsistencyDto 테스트")
class LedgerConsistencyDtoTest {

    @Nested
    @DisplayName("UUID 변환")
    class UuidConversionTest {

        @Test
        @DisplayName("byte[] → UUID 변환")
        void shouldConvertBytesToUuid() {
            // given
            UUID originalId = UUID.randomUUID();
            UUID originalMemberId = UUID.randomUUID();

            LedgerConsistencyDto dto = new LedgerConsistencyDto(
                    uuidToBytes(originalId),
                    uuidToBytes(originalMemberId),
                    1000L, 800L, 200L, false,
                    1000L, 0L, -200L, 0L
            );

            // when & then
            assertThat(dto.ledgerId()).isEqualTo(originalId);
            assertThat(dto.memberUuid()).isEqualTo(originalMemberId);
        }

        @Test
        @DisplayName("null byte[] → null UUID")
        void shouldReturnNullForNullBytes() {
            LedgerConsistencyDto dto = new LedgerConsistencyDto(
                    null, null,
                    1000L, 1000L, 0L, false,
                    1000L, 0L, 0L, 0L
            );

            assertThat(dto.ledgerId()).isNull();
            assertThat(dto.memberUuid()).isNull();
        }

        @Test
        @DisplayName("잘못된 크기 byte[] → null UUID")
        void shouldReturnNullForInvalidBytes() {
            LedgerConsistencyDto dto = new LedgerConsistencyDto(
                    new byte[10], new byte[10],
                    1000L, 1000L, 0L, false,
                    1000L, 0L, 0L, 0L
            );

            assertThat(dto.ledgerId()).isNull();
            assertThat(dto.memberUuid()).isNull();
        }
    }

    @Nested
    @DisplayName("적립 금액 계산")
    class CalculatedEarnedAmountTest {

        @Test
        @DisplayName("EARN만 있는 경우")
        void shouldCalculateWithEarnOnly() {
            LedgerConsistencyDto dto = createDto(1000L, 1000L, 0L, false,
                    1000L, 0L, 0L, 0L);

            assertThat(dto.calculatedEarnedAmount()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("EARN + EARN_CANCEL 있는 경우")
        void shouldCalculateWithEarnAndEarnCancel() {
            // EARN_CANCEL은 음수로 저장됨
            LedgerConsistencyDto dto = createDto(500L, 500L, 0L, false,
                    1000L, -500L, 0L, 0L);

            assertThat(dto.calculatedEarnedAmount()).isEqualTo(500L);
        }
    }

    @Nested
    @DisplayName("사용 금액 계산")
    class CalculatedUsedAmountTest {

        @Test
        @DisplayName("USE만 있는 경우")
        void shouldCalculateWithUseOnly() {
            // USE는 음수로 저장됨
            LedgerConsistencyDto dto = createDto(1000L, 700L, 300L, false,
                    1000L, 0L, -300L, 0L);

            assertThat(dto.calculatedUsedAmount()).isEqualTo(300L);
        }

        @Test
        @DisplayName("USE + USE_CANCEL 있는 경우")
        void shouldCalculateWithUseAndUseCancel() {
            // USE -500, USE_CANCEL +200 → 순 사용액 300
            LedgerConsistencyDto dto = createDto(1000L, 700L, 300L, false,
                    1000L, 0L, -500L, 200L);

            assertThat(dto.calculatedUsedAmount()).isEqualTo(300L);
        }

        @Test
        @DisplayName("USE 없는 경우 0 반환")
        void shouldReturnZeroWhenNoUse() {
            LedgerConsistencyDto dto = createDto(1000L, 1000L, 0L, false,
                    1000L, 0L, 0L, 0L);

            assertThat(dto.calculatedUsedAmount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("사용 가능 금액 계산")
    class CalculatedAvailableAmountTest {

        @Test
        @DisplayName("정상 케이스: 적립 - 사용")
        void shouldCalculateAvailable() {
            LedgerConsistencyDto dto = createDto(1000L, 700L, 300L, false,
                    1000L, 0L, -300L, 0L);

            assertThat(dto.calculatedAvailableAmount()).isEqualTo(700L);
        }

        @Test
        @DisplayName("취소된 Ledger는 0 반환")
        void shouldReturnZeroForCanceledLedger() {
            LedgerConsistencyDto dto = createDto(1000L, 0L, 0L, true,
                    1000L, -1000L, 0L, 0L);

            assertThat(dto.calculatedAvailableAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("전액 사용 후 일부 취소")
        void shouldCalculateAfterPartialCancel() {
            // EARN 1000, USE -1000, USE_CANCEL 300 → available 300
            LedgerConsistencyDto dto = createDto(1000L, 300L, 700L, false,
                    1000L, 0L, -1000L, 300L);

            assertThat(dto.calculatedAvailableAmount()).isEqualTo(300L);
        }
    }

    @Nested
    @DisplayName("복합 시나리오")
    class ComplexScenarioTest {

        @Test
        @DisplayName("적립 → 일부사용 → 적립취소 → 사용취소 시나리오")
        void complexScenario() {
            // 적립 1000 → 사용 500 → 적립취소 200 → 사용취소 100
            // earnedAmount = 1000 - 200 = 800
            // usedAmount = 500 - 100 = 400
            // availableAmount = 800 - 400 = 400
            LedgerConsistencyDto dto = createDto(800L, 400L, 400L, false,
                    1000L, -200L, -500L, 100L);

            assertThat(dto.calculatedEarnedAmount()).isEqualTo(800L);
            assertThat(dto.calculatedUsedAmount()).isEqualTo(400L);
            assertThat(dto.calculatedAvailableAmount()).isEqualTo(400L);
        }
    }

    private LedgerConsistencyDto createDto(
            long earnedAmount, long availableAmount, long usedAmount, boolean isCanceled,
            long entryEarnSum, long entryEarnCancelSum, long entryUseSum, long entryUseCancelSum
    ) {
        return new LedgerConsistencyDto(
                uuidToBytes(UUID.randomUUID()),
                uuidToBytes(UUID.randomUUID()),
                earnedAmount, availableAmount, usedAmount, isCanceled,
                entryEarnSum, entryEarnCancelSum, entryUseSum, entryUseCancelSum
        );
    }

    private byte[] uuidToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }
}
