package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.domain.event.*;
import com.musinsa.pointsystem.fixture.EarnPolicyConfigFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberPoint 이벤트 소싱 테스트")
class MemberPointEventSourcingTest {

    @Nested
    @DisplayName("이벤트로부터 Aggregate 복원 (reconstitute)")
    class ReconstituteTest {

        @Test
        @DisplayName("빈 이벤트 목록 - 새 Aggregate 생성")
        void reconstitute_emptyEvents_createsNewAggregate() {
            UUID memberId = UUID.randomUUID();
            List<PointEvent> events = List.of();

            MemberPoint memberPoint = MemberPoint.reconstitute(memberId, events);

            assertThat(memberPoint.memberId()).isEqualTo(memberId);
            assertThat(memberPoint.totalBalance().getValue()).isEqualTo(0L);
            assertThat(memberPoint.ledgers()).isEmpty();
        }

        @Test
        @DisplayName("적립 이벤트 적용 - 잔액 증가")
        void reconstitute_earnEvent_increasesBalance() {
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

            PointEarnedEvent event = new PointEarnedEvent(
                    UUID.randomUUID(), memberId, ledgerId, 1000L,
                    EarnType.SYSTEM, expiredAt, 1L, LocalDateTime.now()
            );

            MemberPoint memberPoint = MemberPoint.reconstitute(memberId, List.of(event));

            assertThat(memberPoint.totalBalance().getValue()).isEqualTo(1000L);
            assertThat(memberPoint.ledgers()).hasSize(1);
            assertThat(memberPoint.ledgers().get(0).id()).isEqualTo(ledgerId);
        }

        @Test
        @DisplayName("여러 이벤트 순차 적용")
        void reconstitute_multipleEvents_appliesInOrder() {
            UUID memberId = UUID.randomUUID();
            UUID ledgerId1 = UUID.randomUUID();
            UUID ledgerId2 = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

            List<PointEvent> events = List.of(
                    new PointEarnedEvent(UUID.randomUUID(), memberId, ledgerId1, 1000L,
                            EarnType.SYSTEM, expiredAt, 1L, LocalDateTime.now()),
                    new PointEarnedEvent(UUID.randomUUID(), memberId, ledgerId2, 500L,
                            EarnType.MANUAL, expiredAt, 2L, LocalDateTime.now()),
                    new PointUsedEvent(UUID.randomUUID(), memberId, UUID.randomUUID(), 300L,
                            "ORDER-1", List.of(new PointUsedEvent.UsageDetail(ledgerId1, 300L)),
                            3L, LocalDateTime.now())
            );

            MemberPoint memberPoint = MemberPoint.reconstitute(memberId, events);

            assertThat(memberPoint.totalBalance().getValue()).isEqualTo(1200L); // 1000 + 500 - 300
            assertThat(memberPoint.ledgers()).hasSize(2);
        }

        @Test
        @DisplayName("스냅샷 + 이벤트로 복원")
        void reconstitute_fromSnapshot_appliesNewEvents() {
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();

            // 스냅샷: 잔액 1000, ledger 1개
            MemberPointSnapshot snapshot = new MemberPointSnapshot(
                    memberId, 1000L,
                    List.of(new MemberPointSnapshot.LedgerSnapshot(
                            ledgerId, 1000L, 1000L, 0L, EarnType.SYSTEM,
                            null, LocalDateTime.now().plusDays(365), false, LocalDateTime.now()
                    )),
                    1L, LocalDateTime.now()
            );

            // 스냅샷 이후 사용 이벤트
            PointUsedEvent useEvent = new PointUsedEvent(
                    UUID.randomUUID(), memberId, UUID.randomUUID(), 300L,
                    "ORDER-1", List.of(new PointUsedEvent.UsageDetail(ledgerId, 300L)),
                    2L, LocalDateTime.now()
            );

            MemberPoint memberPoint = MemberPoint.reconstitute(snapshot, List.of(useEvent));

            assertThat(memberPoint.totalBalance().getValue()).isEqualTo(700L); // 1000 - 300
        }
    }

    @Nested
    @DisplayName("커맨드 처리 → 이벤트 생성 (process)")
    class ProcessTest {

        @Test
        @DisplayName("적립 처리 - 이벤트 생성")
        void processEarn_createsEvent() {
            UUID memberId = UUID.randomUUID();
            MemberPoint memberPoint = MemberPoint.create(memberId);
            EarnPolicyConfig policy = EarnPolicyConfigFixture.defaultConfig();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

            MemberPoint.EarnEventResult result = memberPoint.processEarn(
                    PointAmount.of(1000L), EarnType.SYSTEM, expiredAt, policy, 0L
            );

            assertThat(result.event()).isNotNull();
            assertThat(result.event().amount()).isEqualTo(1000L);
            assertThat(result.event().getVersion()).isEqualTo(1L);
            assertThat(result.memberPoint().totalBalance().getValue()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("사용 처리 - 이벤트 생성")
        void processUse_createsEvent() {
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

            // 적립 상태 복원
            PointEarnedEvent earnEvent = new PointEarnedEvent(
                    UUID.randomUUID(), memberId, ledgerId, 1000L,
                    EarnType.SYSTEM, expiredAt, 1L, LocalDateTime.now()
            );
            MemberPoint memberPoint = MemberPoint.reconstitute(memberId, List.of(earnEvent));

            MemberPoint.UseEventResult result = memberPoint.processUse(
                    PointAmount.of(300L), "ORDER-123", 1L
            );

            assertThat(result.event()).isNotNull();
            assertThat(result.event().amount()).isEqualTo(300L);
            assertThat(result.event().orderId()).isEqualTo("ORDER-123");
            assertThat(result.event().usageDetails()).hasSize(1);
            assertThat(result.event().getVersion()).isEqualTo(2L);
            assertThat(result.memberPoint().totalBalance().getValue()).isEqualTo(700L);
        }

        @Test
        @DisplayName("적립취소 처리 - 이벤트 생성")
        void processCancelEarn_createsEvent() {
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

            // 적립 상태 복원
            PointEarnedEvent earnEvent = new PointEarnedEvent(
                    UUID.randomUUID(), memberId, ledgerId, 1000L,
                    EarnType.SYSTEM, expiredAt, 1L, LocalDateTime.now()
            );
            MemberPoint memberPoint = MemberPoint.reconstitute(memberId, List.of(earnEvent));

            MemberPoint.CancelEarnEventResult result = memberPoint.processCancelEarn(ledgerId, 1L);

            assertThat(result.event()).isNotNull();
            assertThat(result.event().ledgerId()).isEqualTo(ledgerId);
            assertThat(result.event().canceledAmount()).isEqualTo(1000L);
            assertThat(result.event().getVersion()).isEqualTo(2L);
            assertThat(result.memberPoint().totalBalance().getValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("스냅샷 생성")
    class SnapshotTest {

        @Test
        @DisplayName("스냅샷 생성 및 복원")
        void toSnapshot_createsValidSnapshot() {
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

            PointEarnedEvent earnEvent = new PointEarnedEvent(
                    UUID.randomUUID(), memberId, ledgerId, 1000L,
                    EarnType.SYSTEM, expiredAt, 1L, LocalDateTime.now()
            );
            MemberPoint memberPoint = MemberPoint.reconstitute(memberId, List.of(earnEvent));

            MemberPointSnapshot snapshot = memberPoint.toSnapshot(1L);

            assertThat(snapshot.memberId()).isEqualTo(memberId);
            assertThat(snapshot.totalBalance()).isEqualTo(1000L);
            assertThat(snapshot.version()).isEqualTo(1L);
            assertThat(snapshot.ledgers()).hasSize(1);

            // 스냅샷으로부터 복원
            MemberPoint restored = snapshot.toMemberPoint();
            assertThat(restored.memberId()).isEqualTo(memberId);
            assertThat(restored.totalBalance().getValue()).isEqualTo(1000L);
        }
    }
}
