package com.musinsa.pointsystem.common.time;

import com.musinsa.pointsystem.fixture.FixedTimeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TimeProviderTest {

    @Nested
    @DisplayName("SystemTimeProvider")
    class SystemTimeProviderTest {

        @Test
        @DisplayName("UTC 시간 반환 검증")
        void now_shouldReturnUtcTime() {
            // GIVEN
            SystemTimeProvider provider = new SystemTimeProvider();
            LocalDateTime utcNow = LocalDateTime.now(ZoneOffset.UTC);

            // WHEN
            LocalDateTime result = provider.now();

            // THEN - 1초 이내의 차이 허용
            assertThat(result).isBetween(utcNow.minusSeconds(1), utcNow.plusSeconds(1));
        }

        @Test
        @DisplayName("시스템 시간대와 무관하게 UTC 반환")
        void now_shouldBeIndependentOfSystemTimezone() {
            // GIVEN
            SystemTimeProvider provider = new SystemTimeProvider();

            // WHEN
            LocalDateTime result = provider.now();
            LocalDateTime expectedUtc = LocalDateTime.now(ZoneOffset.UTC);

            // THEN
            // 시스템 시간대(KST 등)와 관계없이 UTC 시간 반환
            assertThat(result.getHour()).isEqualTo(expectedUtc.getHour());
        }
    }

    @Nested
    @DisplayName("FixedTimeProvider")
    class FixedTimeProviderTest {

        @Test
        @DisplayName("고정된 시간 반환")
        void now_shouldReturnFixedTime() {
            // GIVEN
            LocalDateTime fixedTime = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
            FixedTimeProvider provider = FixedTimeProvider.of(fixedTime);

            // WHEN
            LocalDateTime result = provider.now();

            // THEN
            assertThat(result).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("시간 변경 가능")
        void setFixedTime_shouldChangeTime() {
            // GIVEN
            FixedTimeProvider provider = FixedTimeProvider.create();
            LocalDateTime newTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

            // WHEN
            provider.setFixedTime(newTime);

            // THEN
            assertThat(provider.now()).isEqualTo(newTime);
        }

        @Test
        @DisplayName("N일 후로 시간 이동")
        void advanceDays_shouldMoveTimeForward() {
            // GIVEN
            LocalDateTime initialTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            FixedTimeProvider provider = FixedTimeProvider.of(initialTime);

            // WHEN
            provider.advanceDays(30);

            // THEN
            assertThat(provider.now()).isEqualTo(LocalDateTime.of(2024, 1, 31, 0, 0, 0));
        }
    }

    @Nested
    @DisplayName("만료 여부 확인")
    class ExpirationTest {

        @Test
        @DisplayName("만료일이 현재 시간 이전이면 만료")
        void isExpired_shouldReturnTrue_whenExpiredAtIsBeforeNow() {
            // GIVEN
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            FixedTimeProvider provider = FixedTimeProvider.of(now);
            LocalDateTime expiredAt = now.minusDays(1);

            // WHEN
            boolean result = provider.isExpired(expiredAt);

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("만료일이 현재 시간 이후면 미만료")
        void isExpired_shouldReturnFalse_whenExpiredAtIsAfterNow() {
            // GIVEN
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            FixedTimeProvider provider = FixedTimeProvider.of(now);
            LocalDateTime expiredAt = now.plusDays(1);

            // WHEN
            boolean result = provider.isExpired(expiredAt);

            // THEN
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("만료일이 현재 시간과 동일하면 미만료")
        void isExpired_shouldReturnFalse_whenExpiredAtEqualsNow() {
            // GIVEN
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            FixedTimeProvider provider = FixedTimeProvider.of(now);

            // WHEN
            boolean result = provider.isExpired(now);

            // THEN - 동일 시간은 만료되지 않음 (isBefore 사용)
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("타임존 일관성")
    class TimezoneConsistencyTest {

        @Test
        @DisplayName("plusDays는 UTC 기준으로 계산")
        void plusDays_shouldCalculateInUtc() {
            // GIVEN
            LocalDateTime now = LocalDateTime.of(2024, 6, 15, 23, 0, 0); // UTC 23:00
            FixedTimeProvider provider = FixedTimeProvider.of(now);

            // WHEN
            LocalDateTime result = provider.plusDays(1);

            // THEN - UTC 기준 +1일
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 6, 16, 23, 0, 0));
        }

        @Test
        @DisplayName("만료일 계산 시 타임존 영향 없음")
        void expirationCalculation_shouldBeTimezoneIndependent() {
            // GIVEN - 서로 다른 시간대에서 동일한 UTC 시간
            LocalDateTime utcTime = LocalDateTime.of(2024, 6, 15, 15, 0, 0); // UTC 15:00
            FixedTimeProvider provider = FixedTimeProvider.of(utcTime);

            // WHEN - 365일 후 만료일 계산
            LocalDateTime expiredAt = provider.plusDays(365);

            // THEN
            assertThat(expiredAt).isEqualTo(LocalDateTime.of(2025, 6, 15, 15, 0, 0));
        }
    }
}
