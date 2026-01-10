package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.CancelEarnPointCommand;
import com.musinsa.pointsystem.application.dto.CancelEarnPointResult;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyCanceledException;
import com.musinsa.pointsystem.domain.exception.PointLedgerAlreadyUsedException;
import com.musinsa.pointsystem.domain.exception.PointLedgerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

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
            // GIVEN - SQL로 member_id=2001, ledger_id=2001, 잔액 1000원 생성됨
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(2001L)
                    .ledgerId(2001L)
                    .build();

            // WHEN
            CancelEarnPointResult result = cancelEarnPointUseCase.execute(command);

            // THEN
            assertThat(result.getLedgerId()).isEqualTo(2001L);
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getCanceledAmount()).isEqualTo(1000L);
            assertThat(result.getTotalBalance()).isEqualTo(0L);
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
            // GIVEN - SQL로 member_id=2002, ledger_id=2002, 500원 사용됨
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(2002L)
                    .ledgerId(2002L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerAlreadyUsedException.class)
                    .hasMessageContaining("이미 사용된 적립건");
        }

        @Test
        @DisplayName("CE-T03: 전액 사용된 적립건 취소 실패")
        void cancelFullyUsedEarn_shouldThrowException() {
            // GIVEN - SQL로 member_id=2003, ledger_id=2003, 전액 사용됨
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(2003L)
                    .ledgerId(2003L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerAlreadyUsedException.class)
                    .hasMessageContaining("이미 사용된 적립건");
        }

        @Test
        @DisplayName("CE-T04: 이미 취소된 적립건 재취소 실패")
        void cancelAlreadyCanceledEarn_shouldThrowException() {
            // GIVEN - SQL로 member_id=2004, ledger_id=2004, 이미 취소됨
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(2004L)
                    .ledgerId(2004L)
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
            CancelEarnPointCommand command = CancelEarnPointCommand.builder()
                    .memberId(2001L)
                    .ledgerId(99999L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelEarnPointUseCase.execute(command))
                    .isInstanceOf(PointLedgerNotFoundException.class);
        }
    }
}
