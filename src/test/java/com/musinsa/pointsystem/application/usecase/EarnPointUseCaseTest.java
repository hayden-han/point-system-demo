package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import com.musinsa.pointsystem.domain.model.EarnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EarnPointUseCaseTest extends IntegrationTestBase {

    @Autowired
    private EarnPointUseCase earnPointUseCase;

    @Nested
    @DisplayName("정상 케이스")
    class SuccessCases {

        @Test
        @DisplayName("E-T01: 시스템 적립 성공")
        void earnSystemPoint_success() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1001L)
                    .amount(1000L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // WHEN
            EarnPointResult result = earnPointUseCase.execute(command);

            // THEN
            assertThat(result.getLedgerId()).isNotNull();
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(1001L);
            assertThat(result.getEarnedAmount()).isEqualTo(1000L);
            assertThat(result.getTotalBalance()).isEqualTo(1000L);
            assertThat(result.getExpiredAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("E-T02: 수기 적립 성공")
        void earnManualPoint_success() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1002L)
                    .amount(500L)
                    .earnType(EarnType.MANUAL)
                    .build();

            // WHEN
            EarnPointResult result = earnPointUseCase.execute(command);

            // THEN
            assertThat(result.getLedgerId()).isNotNull();
            assertThat(result.getEarnedAmount()).isEqualTo(500L);
            assertThat(result.getTotalBalance()).isEqualTo(500L);
        }

        @Test
        @DisplayName("E-T03: 만료일 지정 적립")
        void earnWithCustomExpiration_success() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1003L)
                    .amount(1000L)
                    .earnType(EarnType.SYSTEM)
                    .expirationDays(30)
                    .build();

            // WHEN
            EarnPointResult result = earnPointUseCase.execute(command);

            // THEN
            assertThat(result.getLedgerId()).isNotNull();
            LocalDateTime expectedExpiration = LocalDateTime.now().plusDays(30);
            assertThat(result.getExpiredAt()).isBefore(expectedExpiration.plusMinutes(1));
            assertThat(result.getExpiredAt()).isAfter(expectedExpiration.minusMinutes(1));
        }

        @Test
        @DisplayName("E-T04: 최소 금액(1원) 적립")
        void earnMinimumAmount_success() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1004L)
                    .amount(1L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // WHEN
            EarnPointResult result = earnPointUseCase.execute(command);

            // THEN
            assertThat(result.getEarnedAmount()).isEqualTo(1L);
            assertThat(result.getTotalBalance()).isEqualTo(1L);
        }

        @Test
        @DisplayName("E-T05: 최대 금액(100,000원) 적립")
        void earnMaximumAmount_success() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1005L)
                    .amount(100000L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // WHEN
            EarnPointResult result = earnPointUseCase.execute(command);

            // THEN
            assertThat(result.getEarnedAmount()).isEqualTo(100000L);
            assertThat(result.getTotalBalance()).isEqualTo(100000L);
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailureCases {

        @Test
        @DisplayName("E-T06: 최소 금액 미만 실패")
        void earnBelowMinimum_shouldThrowException() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1006L)
                    .amount(0L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> earnPointUseCase.execute(command))
                    .isInstanceOf(InvalidEarnAmountException.class)
                    .hasMessageContaining("최소 적립 금액");
        }

        @Test
        @DisplayName("E-T07: 최대 금액 초과 실패")
        void earnAboveMaximum_shouldThrowException() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1007L)
                    .amount(100001L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> earnPointUseCase.execute(command))
                    .isInstanceOf(InvalidEarnAmountException.class)
                    .hasMessageContaining("최대 적립 금액");
        }

        @Test
        @SqlGroup({
                @Sql(scripts = "/sql/earn-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
                @Sql(scripts = "/sql/earn-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        })
        @DisplayName("E-T08: 최대 보유금액 초과 실패")
        void earnExceedMaxBalance_shouldThrowException() {
            // GIVEN - SQL로 이미 9,500,000원 보유한 회원 1001 생성됨
            // 100,000원 적립 6번 시도 (9,500,000 + 600,000 = 10,100,000 > 최대 10,000,000)
            // 1회 최대 적립 금액이 100,000원이므로 5번까지는 성공, 6번째에 실패
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1001L)
                    .amount(100000L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // 5번 적립 (9,500,000 + 500,000 = 10,000,000)
            for (int i = 0; i < 5; i++) {
                earnPointUseCase.execute(command);
            }

            // 6번째 (1원이라도 추가하면 실패)
            EarnPointCommand overflowCommand = EarnPointCommand.builder()
                    .memberId(1001L)
                    .amount(1L)
                    .earnType(EarnType.SYSTEM)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> earnPointUseCase.execute(overflowCommand))
                    .isInstanceOf(MaxBalanceExceededException.class)
                    .hasMessageContaining("최대 보유 가능 금액");
        }

        @Test
        @DisplayName("E-T09: 만료일 1일 미만 실패")
        void earnExpirationBelowMinimum_shouldThrowException() {
            // GIVEN
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1009L)
                    .amount(1000L)
                    .earnType(EarnType.SYSTEM)
                    .expirationDays(0)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> earnPointUseCase.execute(command))
                    .isInstanceOf(InvalidExpirationException.class)
                    .hasMessageContaining("최소 만료일");
        }

        @Test
        @DisplayName("E-T10: 만료일 5년 초과 실패")
        void earnExpirationAboveMaximum_shouldThrowException() {
            // GIVEN - 1825일 = 5년 (1824일이 최대)
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(1010L)
                    .amount(1000L)
                    .earnType(EarnType.SYSTEM)
                    .expirationDays(1825)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> earnPointUseCase.execute(command))
                    .isInstanceOf(InvalidExpirationException.class)
                    .hasMessageContaining("최대 만료일");
        }
    }
}
