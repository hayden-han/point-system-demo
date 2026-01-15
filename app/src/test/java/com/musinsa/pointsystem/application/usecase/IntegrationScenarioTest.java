package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.*;
import com.musinsa.pointsystem.infra.adapter.UuidGenerator;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
import com.musinsa.pointsystem.domain.repository.PointQueryRepository;
import com.musinsa.pointsystem.domain.model.PointRules;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import com.musinsa.pointsystem.infra.persistence.repository.PointLedgerJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationScenarioTest extends IntegrationTestBase {

    @Autowired
    private EarnPointUseCase earnPointUseCase;

    @Autowired
    private UsePointUseCase usePointUseCase;

    @Autowired
    private CancelUsePointUseCase cancelUsePointUseCase;

    @Autowired
    private PointQueryRepository pointQueryRepository;

    @Autowired
    private PointLedgerRepository pointLedgerRepository;

    @Autowired
    private PointLedgerJpaRepository pointLedgerJpaRepository;

    @Nested
    @DisplayName("통합 시나리오 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/integration-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/integration-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class IntegrationCases {

        @Test
        @DisplayName("INT-T01: 요구사항 예시 전체 흐름")
        void fullScenario_success() {
            // GIVEN
            UUID memberId = new UuidGenerator().generate();
            LocalDateTime now = LocalDateTime.now();

            // ===== STEP 1: 1000원 적립 -> 잔액 1000, pointKey A =====
            EarnPointCommand earnCommandA = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(1000L)
                    .earnType("SYSTEM")
                    .build();

            // WHEN
            EarnPointResult earnResultA = earnPointUseCase.execute(earnCommandA);

            // THEN
            assertThat(earnResultA.totalBalance()).isEqualTo(1000L);
            UUID ledgerIdA = earnResultA.ledgerId();

            // ===== STEP 2: 500원 적립 -> 잔액 1500, pointKey B =====
            // GIVEN
            EarnPointCommand earnCommandB = EarnPointCommand.builder()
                    .memberId(memberId)
                    .amount(500L)
                    .earnType("SYSTEM")
                    .build();

            // WHEN
            EarnPointResult earnResultB = earnPointUseCase.execute(earnCommandB);

            // THEN
            assertThat(earnResultB.totalBalance()).isEqualTo(1500L);
            UUID ledgerIdB = earnResultB.ledgerId();

            // ===== STEP 3: 주문 A1234에서 1200원 사용 -> 잔액 300 =====
            // GIVEN
            UsePointCommand useCommand = UsePointCommand.builder()
                    .memberId(memberId)
                    .amount(1200L)
                    .orderId("A1234")
                    .build();

            // WHEN
            UsePointResult useResult = usePointUseCase.execute(useCommand);

            // THEN
            assertThat(useResult.usedAmount()).isEqualTo(1200L);
            assertThat(useResult.totalBalance()).isEqualTo(300L);

            // 적립건 상태 확인
            PointLedger ledgerAAfterUse = pointLedgerRepository.findById(ledgerIdA).orElseThrow();
            assertThat(ledgerAAfterUse.availableAmount()).isEqualTo(0L);

            PointLedger ledgerBAfterUse = pointLedgerRepository.findById(ledgerIdB).orElseThrow();
            assertThat(ledgerBAfterUse.availableAmount()).isEqualTo(300L);

            // ===== STEP 4: A 만료 처리 (테스트용 시간 조작) =====
            // GIVEN
            PointLedgerEntity ledgerEntityA = pointLedgerJpaRepository.findById(ledgerIdA).orElseThrow();
            ledgerEntityA.setExpiredAt(LocalDateTime.now().minusDays(1));
            pointLedgerJpaRepository.save(ledgerEntityA);

            // THEN - A가 만료되었는지 확인
            PointLedger ledgerAExpired = pointLedgerRepository.findById(ledgerIdA).orElseThrow();
            assertThat(PointRules.isExpired(ledgerAExpired, LocalDateTime.now())).isTrue();

            // ===== STEP 5: 1100원 사용취소 -> 잔액 1400 =====
            // GIVEN
            CancelUsePointCommand cancelCommand = CancelUsePointCommand.builder()
                    .memberId(memberId)
                    .orderId("A1234")
                    .cancelAmount(1100L)
                    .build();

            // WHEN
            CancelUsePointResult cancelResult = cancelUsePointUseCase.execute(cancelCommand);

            // THEN
            assertThat(cancelResult.canceledAmount()).isEqualTo(1100L);
            assertThat(cancelResult.totalBalance()).isEqualTo(1400L);

            // ===== STEP 6: 검증 =====
            // 총 잔액: 1400
            long totalBalance = pointQueryRepository.getTotalBalance(memberId, LocalDateTime.now()).getValue();
            assertThat(totalBalance).isEqualTo(1400L);

            // 취소 순서는 ledgersWithOrder(해당 orderId로 취소 가능한 Ledger 목록) 순서에 따름
            // A(만료)에서 1000원 취소 -> 신규 적립건 1000원
            // B(미만료)에서 100원 복구 -> B 잔액 400

            PointLedger ledgerBAfterCancel = pointLedgerRepository.findById(ledgerIdB).orElseThrow();
            assertThat(ledgerBAfterCancel.availableAmount()).isEqualTo(400L);

            // 사용 가능한 적립건 확인 (B 복구 + 신규 생성)
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId, LocalDateTime.now());
            long totalAvailable = availableLedgers.stream()
                    .mapToLong(PointLedger::availableAmount)
                    .sum();
            assertThat(totalAvailable).isEqualTo(1400L);

            // 신규 적립건 확인 (A 만료로 인해 생성됨)
            assertThat(availableLedgers.size()).isGreaterThanOrEqualTo(2);
        }
    }
}
