-- 적립 테스트용 데이터 (UUID 기반)
-- member_id: 1001 ~ 1010

-- 최대 보유금액 테스트용 회원 (E-T08)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000001001', 9500000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- v2: balance는 Ledger에서 계산되므로, 잔액 보유를 위한 Ledger도 추가
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000002001', X'00000000000000000000000000001001', 9500000, 9500000, 0, 'SYSTEM', TIMESTAMPADD(DAY, 365, CURRENT_TIMESTAMP), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
