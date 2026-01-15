package com.musinsa.pointsystem.domain.model;

import com.musinsa.pointsystem.fixture.PointLedgerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PointLedgerTest {

    @Nested
    @DisplayName("적립건 생성")
    class CreateTest {

        @Test
        @DisplayName("기본 적립건 생성")
        void create_shouldInitializeCorrectly() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = now.plusDays(365);

            // WHEN
            PointLedger ledger = PointLedger.create(id, memberId, 1000L, EarnType.SYSTEM, expiredAt, null, now);

            // THEN
            assertThat(ledger.id()).isEqualTo(id);
            assertThat(ledger.memberId()).isEqualTo(memberId);
            assertThat(ledger.earnedAmount()).isEqualTo(1000L);
            assertThat(ledger.availableAmount()).isEqualTo(1000L);
            assertThat(ledger.earnType()).isEqualTo(EarnType.SYSTEM);
            assertThat(ledger.canceled()).isFalse();
        }

        @Test
        @DisplayName("사용취소로 인한 신규 적립건 생성 (sourceLedgerId 포함)")
        void createWithSourceLedgerId_shouldSetSourceLedgerId() {
            // GIVEN
            UUID id = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID sourceLedgerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            PointLedger ledger = PointLedger.create(
                    id, memberId, 500L, EarnType.USE_CANCEL,
                    now.plusDays(365), sourceLedgerId, now
            );

            // THEN
            assertThat(ledger.sourceLedgerId()).isEqualTo(sourceLedgerId);
            assertThat(ledger.availableAmount()).isEqualTo(500L);
            assertThat(ledger.earnType()).isEqualTo(EarnType.USE_CANCEL);
        }
    }

    @Nested
    @DisplayName("적립건 불변성")
    class ImmutabilityTest {

        @Test
        @DisplayName("withAvailableAmount는 새 객체 반환")
        void withAvailableAmount_shouldReturnNewObject() {
            // GIVEN
            PointLedger original = PointLedgerFixture.createSystem(UUID.randomUUID(), UUID.randomUUID(), 1000L);

            // WHEN
            PointLedger updated = original.withAvailableAmount(700L);

            // THEN
            assertThat(updated).isNotSameAs(original);
            assertThat(original.availableAmount()).isEqualTo(1000L);
            assertThat(updated.availableAmount()).isEqualTo(700L);
        }

        @Test
        @DisplayName("withCanceled는 새 객체 반환")
        void withCanceled_shouldReturnNewObject() {
            // GIVEN
            PointLedger original = PointLedgerFixture.createSystem(UUID.randomUUID(), UUID.randomUUID(), 1000L);

            // WHEN
            PointLedger canceled = original.withCanceled();

            // THEN
            assertThat(canceled).isNotSameAs(original);
            assertThat(original.canceled()).isFalse();
            assertThat(canceled.canceled()).isTrue();
            assertThat(canceled.availableAmount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("수기 지급 여부")
    class ManualTypeTest {

        @Test
        @DisplayName("MANUAL 타입이면 true")
        void manualType_isManual() {
            PointLedger ledger = PointLedgerFixture.createManual(UUID.randomUUID(), UUID.randomUUID(), 1000L);

            assertThat(ledger.isManual()).isTrue();
        }

        @Test
        @DisplayName("SYSTEM 타입이면 false")
        void systemType_isNotManual() {
            PointLedger ledger = PointLedgerFixture.createSystem(UUID.randomUUID(), UUID.randomUUID(), 1000L);

            assertThat(ledger.isManual()).isFalse();
        }

        @Test
        @DisplayName("USE_CANCEL 타입이면 false")
        void useCancelType_isNotManual() {
            UUID id = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            PointLedger ledger = PointLedger.create(
                    id, memberId, 1000L, EarnType.USE_CANCEL,
                    now.plusDays(365), UUID.randomUUID(), now
            );

            assertThat(ledger.isManual()).isFalse();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("경계: 최소 금액(1원) 적립건 생성")
        void boundary_minAmount() {
            UUID id = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            PointLedger ledger = PointLedger.create(
                    id, memberId, 1L, EarnType.SYSTEM,
                    now.plusDays(365), null, now
            );

            assertThat(ledger.earnedAmount()).isEqualTo(1L);
            assertThat(ledger.availableAmount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("경계: 전액 차감 시 availableAmount = 0")
        void boundary_useFullAmount() {
            PointLedger ledger = PointLedgerFixture.createSystem(UUID.randomUUID(), UUID.randomUUID(), 1000L);

            PointLedger updated = ledger.withAvailableAmount(0L);

            assertThat(updated.availableAmount()).isEqualTo(0L);
            assertThat(updated.earnedAmount()).isEqualTo(1000L);
        }
    }
}
