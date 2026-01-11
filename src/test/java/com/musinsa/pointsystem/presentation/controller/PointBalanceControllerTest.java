package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.common.util.UuidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SqlGroup({
        @Sql(scripts = "/sql/balance-controller-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/sql/balance-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/sql/balance-controller-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@DisplayName("PointBalanceController API 통합 테스트")
class PointBalanceControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/v1/members/{memberId}/points - 잔액 조회")
    class GetBalance {

        @Test
        @DisplayName("회원의 포인트 잔액을 조회한다")
        void shouldReturnMemberPointBalance() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008101");

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/members/{memberId}/points", memberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.totalBalance").value(5000));
        }

        @Test
        @DisplayName("잔액이 0인 회원의 포인트를 조회한다")
        void shouldReturnZeroBalanceForNewMember() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008102");

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/members/{memberId}/points", memberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.totalBalance").value(0));
        }

        @Test
        @DisplayName("존재하지 않는 회원 조회 시 초기 잔액 0을 반환한다")
        void shouldReturnZeroBalanceForNonExistentMember() throws Exception {
            // GIVEN
            UUID memberId = UuidGenerator.generate();

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/members/{memberId}/points", memberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.totalBalance").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/{memberId}/points/history - 거래 내역 조회")
    class GetHistory {

        @Test
        @DisplayName("회원의 포인트 거래 내역을 조회한다")
        void shouldReturnTransactionHistory() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008101");

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/members/{memberId}/points/history", memberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].memberId").value(memberId.toString()));
        }

        @Test
        @DisplayName("거래 내역이 없는 회원은 빈 목록을 반환한다")
        void shouldReturnEmptyHistoryForMemberWithNoTransactions() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008102");

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/members/{memberId}/points/history", memberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("사용 내역이 있는 회원의 거래 내역을 조회한다")
        void shouldReturnHistoryIncludingUsage() throws Exception {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000008105");

            // WHEN & THEN
            mockMvc.perform(get("/api/v1/members/{memberId}/points/history", memberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }
    }
}
