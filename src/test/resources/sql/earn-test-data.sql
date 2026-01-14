-- 적립 테스트용 데이터 (v2 스키마)
-- member_id: 1001 ~ 1010

-- 최대 보유금액 테스트용 회원 (E-T08)
-- v2: balance는 Ledger에서 계산되므로, Ledger만 생성
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000002001', X'00000000000000000000000000001001', 9500000, 9500000, 0, 'SYSTEM', TIMESTAMPADD(DAY, 365, CURRENT_TIMESTAMP), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020010', X'00000000000000000000000000002001', 'EARN', 9500000, NULL, CURRENT_TIMESTAMP);
