package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.dto.UsePointResult;
import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import com.musinsa.pointsystem.domain.exception.InvalidOrderIdException;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsePointUseCaseTest extends IntegrationTestBase {

    @Autowired
    private UsePointUseCase usePointUseCase;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Nested
    @DisplayName("정상 케이스")
    @SqlGroup({
            @Sql(scripts = "/sql/use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class SuccessCases {

        @Test
        @DisplayName("U-T01: 단일 적립건에서 사용")
        void useSingleLedger_success() {
            // GIVEN - SQL로 member_id=3001, ledger_id=3001, 잔액 1000원 생성됨
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3001L)
                    .amount(500L)
                    .orderId("ORDER-U-T01")
                    .build();

            // WHEN
            UsePointResult result = usePointUseCase.execute(command);

            // THEN
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getUsedAmount()).isEqualTo(500L);
            assertThat(result.getTotalBalance()).isEqualTo(500L);
            assertThat(result.getOrderId()).isEqualTo("ORDER-U-T01");
        }

        @Test
        @DisplayName("U-T02: 여러 적립건에서 사용")
        void useMultipleLedgers_success() {
            // GIVEN - SQL로 member_id=3002, ledger_id=3002(500), 3003(500), 총 1000원 생성됨
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3002L)
                    .amount(800L)
                    .orderId("ORDER-U-T02")
                    .build();

            // WHEN
            UsePointResult result = usePointUseCase.execute(command);

            // THEN
            assertThat(result.getUsedAmount()).isEqualTo(800L);
            assertThat(result.getTotalBalance()).isEqualTo(200L);
        }

        @Test
        @DisplayName("U-T03: 전액 사용")
        void useFullBalance_success() {
            // GIVEN - SQL로 member_id=3002, 총 1000원 생성됨 (U-T02와 별도 테스트)
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3002L)
                    .amount(1000L)
                    .orderId("ORDER-U-T03")
                    .build();

            // WHEN
            UsePointResult result = usePointUseCase.execute(command);

            // THEN
            assertThat(result.getUsedAmount()).isEqualTo(1000L);
            assertThat(result.getTotalBalance()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("우선순위 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class PriorityCases {

        @Test
        @DisplayName("U-T04: 수기 지급 우선 사용")
        void manualFirst_success() {
            // GIVEN - SQL로 member_id=3003, SYSTEM(3004) 500, MANUAL(3005) 500 생성됨
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3003L)
                    .amount(300L)
                    .orderId("ORDER-U-T04")
                    .build();

            // WHEN
            UsePointResult result = usePointUseCase.execute(command);

            // THEN
            assertThat(result.getUsedAmount()).isEqualTo(300L);

            // MANUAL(3005)에서 먼저 사용되었는지 확인
            List<PointLedger> ledgers = pointLedgerRepository.findAvailableByMemberId(3003L);
            PointLedger manualLedger = ledgers.stream()
                    .filter(l -> l.getId().equals(3005L))
                    .findFirst()
                    .orElseThrow();
            PointLedger systemLedger = ledgers.stream()
                    .filter(l -> l.getId().equals(3004L))
                    .findFirst()
                    .orElseThrow();

            assertThat(manualLedger.getAvailableAmount()).isEqualTo(200L); // 500 - 300 = 200
            assertThat(systemLedger.getAvailableAmount()).isEqualTo(500L); // 사용 안됨
        }

        @Test
        @DisplayName("U-T05: 만료일 짧은 순 사용")
        void expirationFirst_success() {
            // GIVEN - SQL로 member_id=3004, 만료 30일(3006) 500, 만료 10일(3007) 500 생성됨
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3004L)
                    .amount(300L)
                    .orderId("ORDER-U-T05")
                    .build();

            // WHEN
            UsePointResult result = usePointUseCase.execute(command);

            // THEN
            assertThat(result.getUsedAmount()).isEqualTo(300L);

            // 만료일 짧은 3007에서 먼저 사용되었는지 확인
            List<PointLedger> ledgers = pointLedgerRepository.findAvailableByMemberId(3004L);
            PointLedger shortExpLedger = ledgers.stream()
                    .filter(l -> l.getId().equals(3007L))
                    .findFirst()
                    .orElseThrow();
            PointLedger longExpLedger = ledgers.stream()
                    .filter(l -> l.getId().equals(3006L))
                    .findFirst()
                    .orElseThrow();

            assertThat(shortExpLedger.getAvailableAmount()).isEqualTo(200L); // 500 - 300 = 200
            assertThat(longExpLedger.getAvailableAmount()).isEqualTo(500L); // 사용 안됨
        }

        @Test
        @DisplayName("U-T06: 수기+만료일 복합 우선순위")
        void manualBeforeExpiration_success() {
            // GIVEN - SQL로 member_id=3005, SYSTEM 10일(3008) 500, MANUAL 30일(3009) 500 생성됨
            // MANUAL이 만료일이 길더라도 MANUAL이 우선
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3005L)
                    .amount(300L)
                    .orderId("ORDER-U-T06")
                    .build();

            // WHEN
            UsePointResult result = usePointUseCase.execute(command);

            // THEN
            assertThat(result.getUsedAmount()).isEqualTo(300L);

            // MANUAL(3009)에서 먼저 사용되었는지 확인
            List<PointLedger> ledgers = pointLedgerRepository.findAvailableByMemberId(3005L);
            PointLedger manualLedger = ledgers.stream()
                    .filter(l -> l.getId().equals(3009L))
                    .findFirst()
                    .orElseThrow();
            PointLedger systemLedger = ledgers.stream()
                    .filter(l -> l.getId().equals(3008L))
                    .findFirst()
                    .orElseThrow();

            assertThat(manualLedger.getAvailableAmount()).isEqualTo(200L); // 500 - 300 = 200
            assertThat(systemLedger.getAvailableAmount()).isEqualTo(500L); // 사용 안됨
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    @SqlGroup({
            @Sql(scripts = "/sql/use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class FailureCases {

        @Test
        @DisplayName("U-T07: 잔액 부족 실패")
        void insufficientBalance_shouldThrowException() {
            // GIVEN - SQL로 member_id=3006, 잔액 500원 생성됨
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3006L)
                    .amount(1000L)
                    .orderId("ORDER-U-T07")
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> usePointUseCase.execute(command))
                    .isInstanceOf(InsufficientPointException.class);
        }

        @Test
        @DisplayName("U-T08: 잔액 0 실패")
        void zeroBalance_shouldThrowException() {
            // GIVEN - SQL로 member_id=3007, 잔액 0원 생성됨
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3007L)
                    .amount(100L)
                    .orderId("ORDER-U-T08")
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> usePointUseCase.execute(command))
                    .isInstanceOf(InsufficientPointException.class);
        }

        @Test
        @DisplayName("U-T09: 주문번호 누락 실패")
        void missingOrderId_shouldThrowException() {
            // GIVEN - 주문번호 없이 사용 시도
            UsePointCommand command = UsePointCommand.builder()
                    .memberId(3001L)
                    .amount(100L)
                    .orderId(null)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> usePointUseCase.execute(command))
                    .isInstanceOf(InvalidOrderIdException.class)
                    .hasMessageContaining("주문번호는 필수");
        }
    }
}
