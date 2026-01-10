package com.musinsa.pointsystem.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.presentation.dto.request.EarnPointRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SqlGroup({
        @Sql(scripts = "/sql/earn-controller-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/sql/earn-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/sql/earn-controller-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@DisplayName("PointEarnController API 통합 테스트")
class PointEarnControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/members/{memberId}/points/earn - 포인트 적립")
    class Earn {

        @Test
        @DisplayName("포인트를 적립한다")
        void shouldEarnPoints() throws Exception {
            // GIVEN
            Long memberId = 8202L;
            EarnPointRequest request = EarnPointRequest.builder()
                    .amount(1000L)
                    .earnType(EarnType.SYSTEM)
                    .expirationDays(365)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId))
                    .andExpect(jsonPath("$.earnedAmount").value(1000))
                    .andExpect(jsonPath("$.ledgerId").isNumber());
        }

        @Test
        @DisplayName("수동 적립을 수행한다")
        void shouldEarnManualPoints() throws Exception {
            // GIVEN
            Long memberId = 8202L;
            EarnPointRequest request = EarnPointRequest.builder()
                    .amount(500L)
                    .earnType(EarnType.MANUAL)
                    .expirationDays(30)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId))
                    .andExpect(jsonPath("$.earnedAmount").value(500));
        }

        @Test
        @DisplayName("적립 금액이 0 이하이면 400을 반환한다")
        void shouldReturn400ForZeroOrNegativeAmount() throws Exception {
            // GIVEN
            Long memberId = 8202L;
            EarnPointRequest request = EarnPointRequest.builder()
                    .amount(0L)
                    .earnType(EarnType.SYSTEM)
                    .expirationDays(365)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/members/{memberId}/points/earn/{ledgerId}/cancel - 적립 취소")
    class CancelEarn {

        @Test
        @DisplayName("미사용 적립건을 취소한다")
        void shouldCancelUnusedEarn() throws Exception {
            // GIVEN
            Long memberId = 8203L;
            Long ledgerId = 8202L;

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn/{ledgerId}/cancel", memberId, ledgerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId))
                    .andExpect(jsonPath("$.canceledAmount").value(1000));
        }

        @Test
        @DisplayName("존재하지 않는 적립건 취소 시 404를 반환한다")
        void shouldReturn404ForNonExistentLedger() throws Exception {
            // GIVEN
            Long memberId = 8203L;
            Long ledgerId = 99999L;

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn/{ledgerId}/cancel", memberId, ledgerId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("이미 취소된 적립건 취소 시 400을 반환한다")
        void shouldReturn400ForAlreadyCanceledLedger() throws Exception {
            // GIVEN
            Long memberId = 8203L;
            Long ledgerId = 8202L;

            // 첫 번째 취소
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn/{ledgerId}/cancel", memberId, ledgerId))
                    .andExpect(status().isOk());

            // WHEN & THEN - 동일 적립건 재취소 시도
            mockMvc.perform(post("/api/v1/members/{memberId}/points/earn/{ledgerId}/cancel", memberId, ledgerId))
                    .andExpect(status().isBadRequest());
        }
    }
}
