package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.*;
import com.musinsa.pointsystem.infra.adapter.UuidGenerator;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointLedgerRepository;
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
    private MemberPointRepository memberPointRepository;

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

            // 적립건 상태 확인 (Entry 포함 조회)
            PointLedger ledgerAAfterUse = pointLedgerRepository.findByIdWithEntries(ledgerIdA).orElseThrow();
            assertThat(ledgerAAfterUse.availableAmount()).isEqualTo(PointAmount.of(0L));
            assertThat(ledgerAAfterUse.usedAmount()).isEqualTo(PointAmount.of(1000L));

            PointLedger ledgerBAfterUse = pointLedgerRepository.findByIdWithEntries(ledgerIdB).orElseThrow();
            assertThat(ledgerBAfterUse.availableAmount()).isEqualTo(PointAmount.of(300L));
            assertThat(ledgerBAfterUse.usedAmount()).isEqualTo(PointAmount.of(200L));

            // ===== STEP 4: A 만료 처리 (테스트용 시간 조작) =====
            // GIVEN
            PointLedgerEntity ledgerEntityA = pointLedgerJpaRepository.findById(ledgerIdA).orElseThrow();
            ledgerEntityA.setExpiredAt(LocalDateTime.now().minusDays(1));
            pointLedgerJpaRepository.save(ledgerEntityA);

            // THEN - A가 만료되었는지 확인
            PointLedger ledgerAExpired = pointLedgerRepository.findByIdWithEntries(ledgerIdA).orElseThrow();
            assertThat(ledgerAExpired.isExpired(LocalDateTime.now())).isTrue();

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
            MemberPoint memberPoint = memberPointRepository.findByMemberIdWithAllLedgersAndEntries(memberId).orElseThrow();
            assertThat(memberPoint.getTotalBalance(LocalDateTime.now())).isEqualTo(PointAmount.of(1400L));

            // 취소 순서는 ledgersWithOrder(해당 orderId로 취소 가능한 Ledger 목록) 순서에 따름
            // 테스트 결과에 따르면 A(만료)부터 취소되어:
            // - A(만료)에서 1000원 취소 -> 신규 적립건 1000원
            // - B(미만료)에서 100원 복구 -> B 잔액 400
            // 총 1100원 취소 완료

            PointLedger ledgerBAfterCancel = pointLedgerRepository.findByIdWithEntries(ledgerIdB).orElseThrow();
            assertThat(ledgerBAfterCancel.availableAmount()).isEqualTo(PointAmount.of(400L));

            // 사용 가능한 적립건 확인 (B 복구 + 신규 생성)
            List<PointLedger> availableLedgers = pointLedgerRepository.findAvailableByMemberId(memberId);
            long totalAvailable = availableLedgers.stream()
                    .map(ledger -> ledger.availableAmount().getValue())
                    .mapToLong(v -> v)
                    .sum();
            assertThat(totalAvailable).isEqualTo(1400L);

            // 신규 적립건 확인 (A 만료로 인해 생성됨)
            assertThat(availableLedgers.size()).isGreaterThanOrEqualTo(2);
        }
    }
}
