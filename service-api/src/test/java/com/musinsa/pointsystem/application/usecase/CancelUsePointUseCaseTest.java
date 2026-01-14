package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.CancelUsePointCommand;
import com.musinsa.pointsystem.application.dto.CancelUsePointResult;
import com.musinsa.pointsystem.domain.exception.InvalidCancelAmountException;
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
            // GIVEN - SQL로 member_id, 사용 1000원 (ORDER-CU-T01) 생성됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            String orderId = "ORDER-CU-T01";
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(1000L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            // transactionId 필드 제거됨 - 생략
            assertThat(result.canceledAmount()).isEqualTo(1000L);
            assertThat(result.totalBalance()).isEqualTo(1000L);
            assertThat(result.orderId()).isEqualTo("ORDER-CU-T01");

            // 적립건 복구 확인 (entries 포함 조회)
            PointLedger ledger = pointLedgerRepository.findByIdWithEntries(ledgerId).orElseThrow();
            assertThat(ledger.availableAmount().getValue()).isEqualTo(1000L);
            assertThat(ledger.usedAmount().getValue()).isEqualTo(0L);
        }

        @Test
        @DisplayName("CU-T02: 부분 사용취소")
        void partialCancel_success() {
            // GIVEN - SQL로 member_id, 사용 1000원 (ORDER-CU-T02) 생성됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004002");
            String orderId = "ORDER-CU-T02";
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000004002");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(500L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(500L);
            assertThat(result.totalBalance()).isEqualTo(500L);

            // 적립건 부분 복구 확인 (entries 포함 조회)
            PointLedger ledger = pointLedgerRepository.findByIdWithEntries(ledgerId).orElseThrow();
            assertThat(ledger.availableAmount().getValue()).isEqualTo(500L);
            assertThat(ledger.usedAmount().getValue()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CU-T03: 여러 적립건 부분 취소")
        void multiLedgerPartialCancel_success() {
            // GIVEN - SQL로 member_id, A 500 사용 + B 300 사용, 총 800원 사용됨
            // 현재 잔액 200 (B에 200 남음), 600 취소 요청
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004003");
            String orderId = "ORDER-CU-T03";
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(600L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(600L);
            assertThat(result.totalBalance()).isEqualTo(800L); // 200 + 600

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
            String orderId = "ORDER-CU-T04";
            UUID ledgerId = UUID.fromString("00000000-0000-0000-0000-000000004005");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(500L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(500L);
            assertThat(result.totalBalance()).isEqualTo(500L);

            // 원본 적립건 복구 확인 (entries 포함 조회)
            PointLedger ledger = pointLedgerRepository.findByIdWithEntries(ledgerId).orElseThrow();
            assertThat(ledger.availableAmount().getValue()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CU-T05: 만료된 적립건 복구 (신규 적립)")
        void expiredRestore_createsNewLedger() {
            // GIVEN - SQL로 member_id, 만료된 적립건에서 500원 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004005");
            String orderId = "ORDER-CU-T05";
            UUID expiredLedgerId = UUID.fromString("00000000-0000-0000-0000-000000004006");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(500L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(500L);
            assertThat(result.totalBalance()).isEqualTo(500L);

            // 신규 적립건 생성 확인 (원본 적립건은 만료 상태 유지)
            PointLedger originalLedger = pointLedgerRepository.findByIdWithEntries(expiredLedgerId).orElseThrow();
            assertThat(originalLedger.isExpired(java.time.LocalDateTime.now())).isTrue();
            assertThat(originalLedger.availableAmount().getValue()).isEqualTo(0L);

            // 신규 적립건이 생성되어 사용 가능
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            assertThat(availableLedgers).hasSize(1);
            assertThat(availableLedgers.get(0).availableAmount().getValue()).isEqualTo(500L);
        }

        @Test
        @DisplayName("CU-T06: 혼합 (만료+미만료) 복구")
        void mixedRestore_success() {
            // GIVEN - SQL로 member_id
            // A(만료): 500원 earned, 500원 used (available 0)
            // B(미만료): 500원 earned, 300원 used (available 200)
            // 총 800원 사용됨 (A 500 + B 300), 현재 잔액 200
            // 800 취소 시: B 300 복구 + A 500 신규 적립
            // 결과: B(500) + 신규(500) = 1000
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004006");
            String orderId = "ORDER-CU-T06";
            UUID expiredLedgerId = UUID.fromString("00000000-0000-0000-0000-000000004007");
            UUID notExpiredLedgerId = UUID.fromString("00000000-0000-0000-0000-000000004008");
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(800L)
                    .build();

            // WHEN
            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            // THEN
            assertThat(result.canceledAmount()).isEqualTo(800L);
            // 취소 후 총 잔액: 기존 200 + 취소 800 = 1000
            assertThat(result.totalBalance()).isEqualTo(1000L);

            // 미만료 적립건 복구 확인 (200 + 300 = 500)
            PointLedger notExpiredLedger = pointLedgerRepository.findByIdWithEntries(notExpiredLedgerId).orElseThrow();
            assertThat(notExpiredLedger.availableAmount().getValue()).isEqualTo(500L);

            // 만료 적립건은 복구되지 않고 그대로 (available 그대로 0)
            PointLedger expiredLedger = pointLedgerRepository.findByIdWithEntries(expiredLedgerId).orElseThrow();
            assertThat(expiredLedger.isExpired(java.time.LocalDateTime.now())).isTrue();
            assertThat(expiredLedger.availableAmount().getValue()).isEqualTo(0L);
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
            // GIVEN - SQL로 member_id, 사용 1000원 (ORDER-CU-T07)
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004007");
            String orderId = "ORDER-CU-T07";
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(1500L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(InvalidCancelAmountException.class)
                    .hasMessageContaining("취소 가능 금액 초과");
        }

        @Test
        @DisplayName("CU-T08: 이미 전액 취소된 건 재취소 실패")
        void alreadyCanceled_shouldThrowException() {
            // GIVEN - SQL로 member_id, 이미 전액 취소됨 (ORDER-CU-T08)
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004008");
            String orderId = "ORDER-CU-T08";
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(100L)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }

        @Test
        @DisplayName("CU-T09: 존재하지 않는 주문 실패")
        void orderNotFound_shouldThrowException() {
            // GIVEN - 존재하지 않는 주문 ID
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            String orderId = "NON-EXISTENT-ORDER";
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(100L)
                    .build();

            // WHEN & THEN - 주문 없으면 InvalidCancelAmountException (취소 가능 금액 0)
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/cancel-use-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/cancel-use-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class BoundaryTest {

        @Test
        @DisplayName("경계: 정확히 1원 취소")
        void boundary_cancelOnePoint() {
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004001");
            String orderId = "ORDER-CU-T01";
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(1L)
                    .build();

            CancelUsePointResult result = cancelUsePointUseCase.execute(command);

            assertThat(result.canceledAmount()).isEqualTo(1L);
            assertThat(result.totalBalance()).isEqualTo(1L); // 0 + 1
        }

        @Test
        @DisplayName("경계: 사용금액보다 1원 더 취소 시 실패")
        void boundary_cancelOneMoreThanUsed() {
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004007");
            String orderId = "ORDER-CU-T07";
            CancelUsePointCommand command = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(1001L) // 1000원 사용했는데 1001원 취소
                    .build();

            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }

        @Test
        @DisplayName("경계: 연속 부분 취소")
        void boundary_consecutivePartialCancel() {
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000004002");
            String orderId = "ORDER-CU-T02";

            // 첫 번째 부분 취소 (500원)
            CancelUsePointCommand command1 = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(500L)
                    .build();
            CancelUsePointResult result1 = cancelUsePointUseCase.execute(command1);
            assertThat(result1.canceledAmount()).isEqualTo(500L);
            assertThat(result1.totalBalance()).isEqualTo(500L);

            // 두 번째 부분 취소 (나머지 500원)
            CancelUsePointCommand command2 = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(500L)
                    .build();
            CancelUsePointResult result2 = cancelUsePointUseCase.execute(command2);
            assertThat(result2.canceledAmount()).isEqualTo(500L);
            assertThat(result2.totalBalance()).isEqualTo(1000L);

            // 더 이상 취소할 금액 없음
            CancelUsePointCommand command3 = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId(orderId)
                    .cancelAmount(1L)
                    .build();
            assertThatThrownBy(() -> cancelUsePointUseCase.execute(command3))
                    .isInstanceOf(InvalidCancelAmountException.class);
        }
    }
}
