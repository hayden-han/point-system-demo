package com.musinsa.pointsystem.infra.persistence.mapper;

import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.EntryType;
import com.musinsa.pointsystem.domain.model.LedgerEntry;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.infra.persistence.entity.LedgerEntryEntity;
import com.musinsa.pointsystem.infra.persistence.entity.PointLedgerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointLedgerMapper 테스트")
class PointLedgerMapperTest {

    private PointLedgerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PointLedgerMapper();
    }

    @Nested
    @DisplayName("Entity → Domain 변환")
    class EntityToDomainTest {

        @Test
        @DisplayName("PointLedgerEntity를 PointLedger 도메인으로 변환")
        void shouldConvertEntityToDomain() {
            // given
            UUID id = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);
            LocalDateTime earnedAt = LocalDateTime.now();

            PointLedgerEntity entity = PointLedgerEntity.builder()
                    .id(id)
                    .memberId(memberId)
                    .earnedAmount(1000L)
                    .availableAmount(800L)
                    .usedAmount(200L)
                    .earnType("MANUAL")
                    .sourceLedgerId(null)
                    .expiredAt(expiredAt)
                    .isCanceled(false)
                    .earnedAt(earnedAt)
                    .build();

            // when
            PointLedger domain = mapper.toDomain(entity);

            // then
            assertThat(domain.id()).isEqualTo(id);
            assertThat(domain.memberId()).isEqualTo(memberId);
            assertThat(domain.earnedAmount()).isEqualTo(1000L);
            assertThat(domain.availableAmount()).isEqualTo(800L);
            assertThat(domain.earnType()).isEqualTo(EarnType.MANUAL);
            assertThat(domain.expiredAt()).isEqualTo(expiredAt);
            assertThat(domain.canceled()).isFalse();
            assertThat(domain.earnedAt()).isEqualTo(earnedAt);
        }

        @Test
        @DisplayName("취소된 Entity 변환")
        void shouldConvertCanceledEntity() {
            // given
            PointLedgerEntity entity = PointLedgerEntity.builder()
                    .id(UUID.randomUUID())
                    .memberId(UUID.randomUUID())
                    .earnedAmount(1000L)
                    .availableAmount(0L)
                    .usedAmount(0L)
                    .earnType("MANUAL")
                    .expiredAt(LocalDateTime.now().plusDays(365))
                    .isCanceled(true)
                    .earnedAt(LocalDateTime.now())
                    .build();

            // when
            PointLedger domain = mapper.toDomain(entity);

            // then
            assertThat(domain.canceled()).isTrue();
            assertThat(domain.availableAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("sourceLedgerId가 있는 Entity 변환")
        void shouldConvertEntityWithSourceLedgerId() {
            // given
            UUID sourceLedgerId = UUID.randomUUID();
            PointLedgerEntity entity = PointLedgerEntity.builder()
                    .id(UUID.randomUUID())
                    .memberId(UUID.randomUUID())
                    .earnedAmount(500L)
                    .availableAmount(500L)
                    .usedAmount(0L)
                    .earnType("MANUAL")
                    .sourceLedgerId(sourceLedgerId)
                    .expiredAt(LocalDateTime.now().plusDays(365))
                    .isCanceled(false)
                    .earnedAt(LocalDateTime.now())
                    .build();

            // when
            PointLedger domain = mapper.toDomain(entity);

            // then
            assertThat(domain.sourceLedgerId()).isEqualTo(sourceLedgerId);
        }
    }

    @Nested
    @DisplayName("Domain → Entity 변환")
    class DomainToEntityTest {

        @Test
        @DisplayName("PointLedger 도메인을 Entity로 변환")
        void shouldConvertDomainToEntity() {
            // given
            UUID id = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);
            LocalDateTime earnedAt = LocalDateTime.now();

            PointLedger domain = new PointLedger(
                    id,
                    memberId,
                    1000L,
                    700L,
                    EarnType.SYSTEM,
                    null,
                    expiredAt,
                    false,
                    earnedAt
            );

            // when
            PointLedgerEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getMemberId()).isEqualTo(memberId);
            assertThat(entity.getEarnedAmount()).isEqualTo(1000L);
            assertThat(entity.getAvailableAmount()).isEqualTo(700L);
            assertThat(entity.getUsedAmount()).isEqualTo(300L);
            assertThat(entity.getEarnType()).isEqualTo("SYSTEM");
            assertThat(entity.getExpiredAt()).isEqualTo(expiredAt);
            assertThat(entity.getIsCanceled()).isFalse();
            assertThat(entity.getEarnedAt()).isEqualTo(earnedAt);
        }
    }

    @Nested
    @DisplayName("LedgerEntry 변환")
    class EntryConversionTest {

        @Test
        @DisplayName("LedgerEntryEntity를 LedgerEntry로 변환")
        void shouldConvertEntryEntityToDomain() {
            // given
            UUID entryId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now();

            LedgerEntryEntity entity = LedgerEntryEntity.builder()
                    .id(entryId)
                    .ledgerId(ledgerId)
                    .type(EntryType.USE)
                    .amount(-500L)
                    .orderId("ORDER-123")
                    .createdAt(createdAt)
                    .build();

            // when
            LedgerEntry domain = mapper.toEntryDomain(entity);

            // then
            assertThat(domain.id()).isEqualTo(entryId);
            assertThat(domain.ledgerId()).isEqualTo(ledgerId);
            assertThat(domain.type()).isEqualTo(EntryType.USE);
            assertThat(domain.amount()).isEqualTo(-500L);
            assertThat(domain.orderId()).isEqualTo("ORDER-123");
            assertThat(domain.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("LedgerEntry를 LedgerEntryEntity로 변환")
        void shouldConvertEntryDomainToEntity() {
            // given
            UUID entryId = UUID.randomUUID();
            UUID ledgerId = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now();

            LedgerEntry domain = new LedgerEntry(
                    entryId,
                    ledgerId,
                    EntryType.EARN,
                    1000L,
                    null,
                    createdAt
            );

            // when
            LedgerEntryEntity entity = mapper.toEntryEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(entryId);
            assertThat(entity.getLedgerId()).isEqualTo(ledgerId);
            assertThat(entity.getType()).isEqualTo(EntryType.EARN);
            assertThat(entity.getAmount()).isEqualTo(1000L);
            assertThat(entity.getOrderId()).isNull();
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("USE_CANCEL Entry 변환")
        void shouldConvertUseCancelEntry() {
            // given
            LedgerEntryEntity entity = LedgerEntryEntity.builder()
                    .id(UUID.randomUUID())
                    .ledgerId(UUID.randomUUID())
                    .type(EntryType.USE_CANCEL)
                    .amount(300L)  // 양수 (환불)
                    .orderId("ORDER-456")
                    .createdAt(LocalDateTime.now())
                    .build();

            // when
            LedgerEntry domain = mapper.toEntryDomain(entity);

            // then
            assertThat(domain.type()).isEqualTo(EntryType.USE_CANCEL);
            assertThat(domain.amount()).isEqualTo(300L);
        }
    }
}
