package com.musinsa.pointsystem.domain.event;

import com.musinsa.pointsystem.domain.model.EarnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("포인트 도메인 이벤트 테스트")
class PointEventTest {

    @Nested
    @DisplayName("PointEarnedEvent")
    class PointEarnedEventTest {

        @Test
        @DisplayName("적립 이벤트 생성 및 속성 확인")
        void createEarnedEvent() {
            UUID eventId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);
            LocalDateTime occurredAt = LocalDateTime.now();

            PointEarnedEvent event = new PointEarnedEvent(
                    eventId, memberId, ledgerId, 1000L,
                    EarnType.SYSTEM, expiredAt, 1L, occurredAt
            );

            assertThat(event.getEventId()).isEqualTo(eventId);
            assertThat(event.getAggregateId()).isEqualTo(memberId);
            assertThat(event.ledgerId()).isEqualTo(ledgerId);
            assertThat(event.amount()).isEqualTo(1000L);
            assertThat(event.earnType()).isEqualTo(EarnType.SYSTEM);
            assertThat(event.getVersion()).isEqualTo(1L);
            assertThat(event.getEventType()).isEqualTo("POINT_EARNED");
        }
    }

    @Nested
    @DisplayName("PointUsedEvent")
    class PointUsedEventTest {

        @Test
        @DisplayName("사용 이벤트 생성 및 속성 확인")
        void createUsedEvent() {
            UUID eventId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID ledgerId1 = UUID.randomUUID();
            UUID ledgerId2 = UUID.randomUUID();
            LocalDateTime occurredAt = LocalDateTime.now();

            List<PointUsedEvent.UsageDetail> usageDetails = List.of(
                    new PointUsedEvent.UsageDetail(ledgerId1, 300L),
                    new PointUsedEvent.UsageDetail(ledgerId2, 200L)
            );

            PointUsedEvent event = new PointUsedEvent(
                    eventId, memberId, transactionId, 500L,
                    "ORDER-123", usageDetails, 2L, occurredAt
            );

            assertThat(event.getEventId()).isEqualTo(eventId);
            assertThat(event.getAggregateId()).isEqualTo(memberId);
            assertThat(event.transactionId()).isEqualTo(transactionId);
            assertThat(event.amount()).isEqualTo(500L);
            assertThat(event.orderId()).isEqualTo("ORDER-123");
            assertThat(event.usageDetails()).hasSize(2);
            assertThat(event.getVersion()).isEqualTo(2L);
            assertThat(event.getEventType()).isEqualTo("POINT_USED");
        }
    }

    @Nested
    @DisplayName("PointEarnCanceledEvent")
    class PointEarnCanceledEventTest {

        @Test
        @DisplayName("적립취소 이벤트 생성 및 속성 확인")
        void createEarnCanceledEvent() {
            UUID eventId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime occurredAt = LocalDateTime.now();

            PointEarnCanceledEvent event = new PointEarnCanceledEvent(
                    eventId, memberId, ledgerId, 1000L, 3L, occurredAt
            );

            assertThat(event.getEventId()).isEqualTo(eventId);
            assertThat(event.getAggregateId()).isEqualTo(memberId);
            assertThat(event.ledgerId()).isEqualTo(ledgerId);
            assertThat(event.canceledAmount()).isEqualTo(1000L);
            assertThat(event.getVersion()).isEqualTo(3L);
            assertThat(event.getEventType()).isEqualTo("POINT_EARN_CANCELED");
        }
    }

    @Nested
    @DisplayName("PointUseCanceledEvent")
    class PointUseCanceledEventTest {

        @Test
        @DisplayName("사용취소 이벤트 생성 및 속성 확인")
        void createUseCanceledEvent() {
            UUID eventId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID originalTransactionId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            UUID newLedgerId = UUID.randomUUID();
            LocalDateTime occurredAt = LocalDateTime.now();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

            List<PointUseCanceledEvent.RestoredLedger> restored = List.of(
                    new PointUseCanceledEvent.RestoredLedger(ledgerId, 300L)
            );
            List<PointUseCanceledEvent.NewLedger> newLedgers = List.of(
                    new PointUseCanceledEvent.NewLedger(newLedgerId, 200L, EarnType.SYSTEM, expiredAt)
            );

            PointUseCanceledEvent event = new PointUseCanceledEvent(
                    eventId, memberId, originalTransactionId, 500L,
                    "ORDER-123", restored, newLedgers, 4L, occurredAt
            );

            assertThat(event.getEventId()).isEqualTo(eventId);
            assertThat(event.getAggregateId()).isEqualTo(memberId);
            assertThat(event.originalTransactionId()).isEqualTo(originalTransactionId);
            assertThat(event.canceledAmount()).isEqualTo(500L);
            assertThat(event.orderId()).isEqualTo("ORDER-123");
            assertThat(event.restoredLedgers()).hasSize(1);
            assertThat(event.newLedgers()).hasSize(1);
            assertThat(event.getVersion()).isEqualTo(4L);
            assertThat(event.getEventType()).isEqualTo("POINT_USE_CANCELED");
        }
    }

    @Nested
    @DisplayName("Sealed Interface")
    class SealedInterfaceTest {

        @Test
        @DisplayName("sealed interface로 허용된 타입만 존재")
        void sealedInterfacePermits() {
            PointEvent earnEvent = new PointEarnedEvent(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    1000L, EarnType.SYSTEM, LocalDateTime.now().plusDays(365),
                    1L, LocalDateTime.now()
            );

            // sealed interface의 패턴 매칭
            String result = switch (earnEvent) {
                case PointEarnedEvent e -> "earned";
                case PointUsedEvent e -> "used";
                case PointEarnCanceledEvent e -> "earn_canceled";
                case PointUseCanceledEvent e -> "use_canceled";
            };

            assertThat(result).isEqualTo("earned");
        }
    }
}
