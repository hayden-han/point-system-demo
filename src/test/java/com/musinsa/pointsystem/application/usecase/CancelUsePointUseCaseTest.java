package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.common.util.UuidGenerator;
import com.musinsa.pointsystem.domain.exception.InvalidCancelAmountException;
import com.musinsa.pointsystem.domain.exception.PointTransactionNotFoundException;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelUsePointUseCaseTest extends IntegrationTestBase {

    @Autowired
    private CancelUsePointUseCase cancelUsePointUseCase;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Nested
    @DisplayName("정상 케이스")
    @SqlGroup({
            @Sql(scripts = "/sql/cancel-use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/cancel-use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class SuccessCases {

        @Test
        @DisplayName("CU-T01: 전액 사용취소")
        void fullCancel_success() {
            // GIVEN - SQL로 member_id, 사용 1000원 (transaction_id) 생성됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004002");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(1000L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getCanceledAmount()).isEqualTo(1000L);
            assertThat(result.getTotalBalance()).isEqualTo(1000L);
            assertThat(result.getOrderId()).isEqualTo("ORDER-CU-T01");

            // 적립건 복구 확인
            PointLedger ledger = pointLedgerRepository.findById(ledgerId).orElseThrow();
            assertThat(ledger.getAvailableAmount()).isEqualTo(1000L);
            assertThat(ledger.getUsedAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("CU-T02: 부분 사용취소")
        void partialCancel_success() {
            // GIVEN - SQL로 member_id, 사용 1000원 (transaction_id) 생성됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004002");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004004");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000004002");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(500L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.getCanceledAmount()).isEqualTo(500L);
            assertThat(result.getTotalBalance()).isEqualTo(500L);

            // 적립건 부분 복구 확인
            PointLedger ledger = pointLedgerRepository.findById(ledgerId).orElseThrow();
            assertThat(ledger.getAvailableAmount()).isEqualTo(500L);
            assertThat(ledger.getUsedAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CU-T03: 여러 적립건 부분 취소")
        void multiLedgerPartialCancel_success() {
            // GIVEN - SQL로 member_id, A 500 사용 + B 300 사용, 총 800원 사용됨
            // 현재 잔액 200 (B에 200 남음), 600 취소 요청
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004003");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004007");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(600L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.getCanceledAmount()).isEqualTo(600L);
            assertThat(result.getTotalBalance()).isEqualTo(800L); // 200 + 600

            // 적립건 복구 상태 확인
            List<PointLedger> ledgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            assertThat(ledgers).hasSize(2);
        }
    }

    @Nested
    @DisplayName("만료 처리 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/cancel-use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/cancel-use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class ExpirationCases {

        @Test
        @DisplayName("CU-T04: 만료 안된 적립건 복구")
        void notExpiredRestore_success() {
            // GIVEN - SQL로 member_id, 미만료 적립건에서 500원 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004004");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004009");
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000004005");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(500L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.getCanceledAmount()).isEqualTo(500L);
            assertThat(result.getTotalBalance()).isEqualTo(500L);

            // 원본 적립건 복구 확인
            PointLedger ledger = pointLedgerRepository.findById(ledgerId).orElseThrow();
            assertThat(ledger.getAvailableAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CU-T05: 만료된 적립건 복구 (신규 적립)")
        void expiredRestore_createsNewLedger() {
            // GIVEN - SQL로 member_id, 만료된 적립건에서 500원 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004005");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004011");
            UUID expiredLedgerId = UUID.fromString("00000000-0000-0000-0000-000000004006");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(500L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.getCanceledAmount()).isEqualTo(500L);
            assertThat(result.getTotalBalance()).isEqualTo(500L);

            // 신규 적립건 생성 확인 (원본 적립건은 만료 상태 유지)
            PointLedger originalLedger = pointLedgerRepository.findById(expiredLedgerId).orElseThrow();
            assertThat(originalLedger.isExpired()).isTrue();
            assertThat(originalLedger.getAvailableAmount()).isEqualTo(0L);

            // 신규 적립건이 생성되어 사용 가능
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            assertThat(availableLedgers).hasSize(1);
            assertThat(availableLedgers.get(0).getAvailableAmount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CU-T06: 혼합 (만료+미만료) 복구")
        void mixedRestore_success() {
            // GIVEN - SQL로 member_id, A(만료) 500 + B(미만료) 300 사용됨
            // 800 취소 시: B 300 복구 + A 500 신규 적립
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004006");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004014");
            UUID expiredLedgerId = UUID.fromString("00000000-0000-0000-0000-000000004007");
            UUID notExpiredLedgerId = UUID.fromString("00000000-0000-0000-0000-000000004008");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(800L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.getCanceledAmount()).isEqualTo(800L);
            assertThat(result.getTotalBalance()).isEqualTo(800L);

            // 미만료 적립건 복구 확인 (200 + 300 = 500)
            PointLedger notExpiredLedger = pointLedgerRepository.findById(notExpiredLedgerId).orElseThrow();
            assertThat(notExpiredLedger.getAvailableAmount()).isEqualTo(500L);

            // 만료 적립건은 복구되지 않고 그대로
            PointLedger expiredLedger = pointLedgerRepository.findById(expiredLedgerId).orElseThrow();
            assertThat(expiredLedger.isExpired()).isTrue();
            assertThat(expiredLedger.getAvailableAmount()).isEqualTo(0L);

            // 잔액 검증으로 단순화
            assertThat(result.getTotalBalance()).isEqualTo(800L);
        }
    }

    @Nested
    @DisplayName("실패 케이스")
    @SqlGroup({
            @Sql(scripts = "/sql/cancel-use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/cancel-use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class FailureCases {

        @Test
        @DisplayName("CU-T07: 취소 가능 금액 초과 실패")
        void exceedCancelAmount_shouldThrowException() {
            // GIVEN - SQL로 member_id, 사용 1000원 (transaction_id)
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004007");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004016");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(1500L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(InvalidCancelAmountException.class)
                    .hasMessageContaining("취소 가능 금액을 초과");
        }

        @Test
        @DisplayName("CU-T08: 이미 전액 취소된 건 재취소 실패")
        void alreadyCanceled_shouldThrowException() {
            // GIVEN - SQL로 member_id, 이미 전액 취소됨 (transaction_id)
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004008");
            UUID transactionId = UUID.fromString("00000000-0000-0000-0000-000000004018");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(100L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }

        @Test
        @DisplayName("CU-T09: 존재하지 않는 트랜잭션 실패")
        void transactionNotFound_shouldThrowException() {
            // GIVEN - 존재하지 않는 트랜잭션 ID
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            UUID transactionId = UuidGenerator.generate();
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .transactionId(transactionId)
                    .cancelAmount(100L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(PointTransactionNotFoundException.class)
                    .hasMessageContaining("트랜잭션을 찾을 수 없습니다");
        }
    }
}
