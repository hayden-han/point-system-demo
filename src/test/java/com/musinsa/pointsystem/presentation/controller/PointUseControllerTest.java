package com.musinsa.pointsystem.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.presentation.dto.request.CancelUsePointRequest;
import com.musinsa.pointsystem.presentation.dto.request.UsePointRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SqlGroup({
        @Sql(scripts = "/sql/use-controller-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/sql/use-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/sql/use-controller-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@DisplayName("PointUseController API 통합 테스트")
class PointUseControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/members/{memberId}/points/use - 포인트 사용")
    class Use {

        @Test
        @DisplayName("포인트를 사용한다")
        void shouldUsePoints() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008304");
            UsePointRequest request = UsePointRequest.builder()
                    .amount(1000L)
                    .orderId("ORDER-TEST-001")
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.usedAmount").value(1000))
                    .andExpect(jsonPath("$.totalBalance").value(2000));
        }

        @Test
        @DisplayName("잔액 전체를 사용한다")
        void shouldUseAllBalance() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008304");
            UsePointRequest request = UsePointRequest.builder()
                    .amount(3000L)
                    .orderId("ORDER-TEST-002")
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.usedAmount").value(3000))
                    .andExpect(jsonPath("$.totalBalance").value(0));
        }

        @Test
        @DisplayName("잔액 초과 사용 시 400을 반환한다")
        void shouldReturn400ForInsufficientBalance() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008304");
            UsePointRequest request = UsePointRequest.builder()
                    .amount(5000L)
                    .orderId("ORDER-TEST-003")
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("사용 금액이 0 이하이면 400을 반환한다")
        void shouldReturn400ForZeroOrNegativeAmount() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008304");
            UsePointRequest request = UsePointRequest.builder()
                    .amount(0L)
                    .orderId("ORDER-TEST-004")
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("주문번호가 없으면 400을 반환한다")
        void shouldReturn400ForMissingOrderId() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008304");
            UsePointRequest request = UsePointRequest.builder()
                    .amount(1000L)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/members/{memberId}/points/use/cancel - 사용 취소")
    class CancelUse {

        @Test
        @DisplayName("포인트 사용을 전액 취소한다")
        void shouldCancelFullUsage() throws Exception {
            // GIVEN - ORDER-USE-CTRL-TEST로 2000원 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008305");
            CancelUsePointRequest request = CancelUsePointRequest.builder()
                    .orderId("ORDER-USE-CTRL-TEST")
                    .cancelAmount(2000L)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use/cancel", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.canceledAmount").value(2000))
                    .andExpect(jsonPath("$.totalBalance").value(2000));
        }

        @Test
        @DisplayName("포인트 사용을 부분 취소한다")
        void shouldCancelPartialUsage() throws Exception {
            // GIVEN - ORDER-USE-CTRL-TEST로 2000원 사용됨
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008305");
            CancelUsePointRequest request = CancelUsePointRequest.builder()
                    .orderId("ORDER-USE-CTRL-TEST")
                    .cancelAmount(1000L)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use/cancel", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.canceledAmount").value(1000))
                    .andExpect(jsonPath("$.totalBalance").value(1000));
        }

        @Test
        @DisplayName("존재하지 않는 주문 취소 시 404를 반환한다")
        void shouldReturn404ForNonExistentOrder() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008305");
            CancelUsePointRequest request = CancelUsePointRequest.builder()
                    .orderId("NON-EXISTENT-ORDER")
                    .cancelAmount(1000L)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use/cancel", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("취소 금액이 0 이하이면 400을 반환한다")
        void shouldReturn400ForZeroOrNegativeCancelAmount() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008305");
            CancelUsePointRequest request = CancelUsePointRequest.builder()
                    .orderId("ORDER-USE-CTRL-TEST")
                    .cancelAmount(0L)
                    .build();

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/members/{memberId}/points/use/cancel", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
