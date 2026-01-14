package com.musinsa.pointsystem.batch.job.consistency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsistencyCheckResult 테스트")
class ConsistencyCheckResultTest {

    @Test
    @DisplayName("consistent 팩토리 메서드로 일치 결과 생성")
    void shouldCreateConsistentResult() {
        // given
        UUID ledgerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        // when
        ConsistencyCheckResult result = ConsistencyCheckResult.consistent(ledgerId, memberId);

        // then
        assertThat(result.isConsistent()).isTrue();
        assertThat(result.ledgerId()).isEqualTo(ledgerId);
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.NONE);
        assertThat(result.details()).isNull();
    }

    @Test
    @DisplayName("inconsistent 팩토리 메서드로 불일치 결과 생성")
    void shouldCreateInconsistentResult() {
        // given
        UUID ledgerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        String details = "availableAmount: stored=100, calculated=200";

        // when
        ConsistencyCheckResult result = ConsistencyCheckResult.inconsistent(
                ledgerId, memberId,
                ConsistencyCheckResult.InconsistencyType.AVAILABLE_AMOUNT_MISMATCH,
                details
        );

        // then
        assertThat(result.isConsistent()).isFalse();
        assertThat(result.ledgerId()).isEqualTo(ledgerId);
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.type()).isEqualTo(ConsistencyCheckResult.InconsistencyType.AVAILABLE_AMOUNT_MISMATCH);
        assertThat(result.details()).isEqualTo(details);
    }

    @Test
    @DisplayName("InconsistencyType enum 값 확인")
    void shouldHaveCorrectInconsistencyTypes() {
        assertThat(ConsistencyCheckResult.InconsistencyType.values())
                .containsExactlyInAnyOrder(
                        ConsistencyCheckResult.InconsistencyType.NONE,
                        ConsistencyCheckResult.InconsistencyType.AVAILABLE_AMOUNT_MISMATCH,
                        ConsistencyCheckResult.InconsistencyType.USED_AMOUNT_MISMATCH,
                        ConsistencyCheckResult.InconsistencyType.EARNED_AMOUNT_MISMATCH,
                        ConsistencyCheckResult.InconsistencyType.MULTIPLE_MISMATCHES
                );
    }
}
