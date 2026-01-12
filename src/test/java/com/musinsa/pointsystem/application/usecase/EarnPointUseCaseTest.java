package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.InvalidEarnAmountException;
import com.musinsa.pointsystem.domain.exception.InvalidExpirationException;
import com.musinsa.pointsystem.domain.exception.MaxBalanceExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDateTime;
import java.util.UUID;

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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1000L)
                    .earnType("SYSTEM")
                    .build();

            // WHEN
            EarnPointResult result = earnPointUseCase.execute(command);

            // THEN
            assertThat(result.getLedgerId()).isNotNull();
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getEarnedAmount()).isEqualTo(1000L);
            assertThat(result.getTotalBalance()).isEqualTo(1000L);
            assertThat(result.getExpiredAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("E-T02: 수기 적립 성공")
        void earnManualPoint_success() {
            // GIVEN
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(500L)
                    .earnType("MANUAL")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1000L)
                    .earnType("SYSTEM")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1L)
                    .earnType("SYSTEM")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(100000L)
                    .earnType("SYSTEM")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(0L)
                    .earnType("SYSTEM")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(100001L)
                    .earnType("SYSTEM")
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
            // GIVEN - SQL로 이미 9,500,000원 보유한 회원 생성됨
            // 100,000원 적립 6번 시도 (9,500,000 + 600,000 = 10,100,000 > 최대 10,000,000)
            // 1회 최대 적립 금액이 100,000원이므로 5번까지는 성공, 6번째에 실패
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000001001");
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(100000L)
                    .earnType("SYSTEM")
                    .build();

            // 5번 적립 (9,500,000 + 500,000 = 10,000,000)
            for (int i = 0; i < 5; i++) {
                earnPointUseCase.execute(command);
            }

            // 6번째 (1원이라도 추가하면 실패)
            EarnPointCommand overflowCommand = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1L)
                    .earnType("SYSTEM")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1000L)
                    .earnType("SYSTEM")
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
            UUID memberId = UuidGenerator.generate();
            EarnPointCommand command = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1000L)
                    .earnType("SYSTEM")
                    .expirationDays(1825)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> earnPointUseCase.execute(command))
                    .isInstanceOf(InvalidExpirationException.class)
                    .hasMessageContaining("최대 만료일");
        }
    }
}
