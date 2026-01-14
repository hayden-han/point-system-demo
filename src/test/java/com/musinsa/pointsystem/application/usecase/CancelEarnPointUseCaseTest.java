package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyCanceledException;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;
import com.musinsa.pointsystem.domain.exception.PointLedgerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelEarnPointUseCaseTest extends IntegrationTestBase {

    @Autowired
    private CancelEarnPointUseCase cancelEarnPointUseCase;

    @Nested
    @DisplayName("정상 케이스")
    @SqlGroup({
            @Sql(scripts = "/sql/cancel-earn-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/cancel-earn-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class SuccessCases {

        @Test
        @DisplayName("CE-T01: 미사용 적립건 취소 성공")
        void cancelUnusedEarn_success() {
            // GIVEN - SQL로 member_id, ledger_id, 잔액 1000원 생성됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000002001");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000002001");
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(memberId)
                    .ledgerId(ledgerId)
                    .build();

            // WHEN
            CancelEarnPointResult result = cancelEarnPointUseCase.execute(command);

            // THEN
            assertThat(result.ledgerId()).isEqualTo(ledgerId);
            assertThat(result.canceledAmount()).isEqualTo(1000L);
            assertThat(result.totalBalance()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    @SqlGroup({
            @Sql(scripts = "/sql/cancel-earn-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/cancel-earn-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class FailureCases {

        @Test
        @DisplayName("CE-T02: 일부 사용된 적립건 취소 실패")
        void cancelPartiallyUsedEarn_shouldThrowException() {
            // GIVEN - SQL로 member_id, ledger_id, 500원 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000002002");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000002002");
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(memberId)
                    .ledgerId(ledgerId)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerAlreadyUsedException.class)
                    .hasMessageContaining("이미 사용된 적립건");
        }

        @Test
        @DisplayName("CE-T03: 전액 사용된 적립건 취소 실패")
        void cancelFullyUsedEarn_shouldThrowException() {
            // GIVEN - SQL로 member_id, ledger_id, 전액 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000002003");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000002003");
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(memberId)
                    .ledgerId(ledgerId)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerAlreadyUsedException.class)
                    .hasMessageContaining("이미 사용된 적립건");
        }

        @Test
        @DisplayName("CE-T04: 이미 취소된 적립건 재취소 실패")
        void cancelAlreadyCanceledEarn_shouldThrowException() {
            // GIVEN - SQL로 member_id, ledger_id, 이미 취소됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000002004");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000002004");
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(memberId)
                    .ledgerId(ledgerId)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerAlreadyCanceledException.class)
                    .hasMessageContaining("이미 취소된 적립건");
        }

        @Test
        @DisplayName("CE-T05: 존재하지 않는 적립건 취소 실패")
        void cancelNonExistentEarn_shouldThrowException() {
            // GIVEN - 존재하지 않는 ledgerId
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000002001");
            UUID ledgerId = UuidGenerator.generate();
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(memberId)
                    .ledgerId(ledgerId)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerNotFoundException.class);
        }
    }
}
