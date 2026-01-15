-- PointEarnController 테스트용 데이터 

-- 적립 테스트용 신규 회원 (8202) - 데이터 없음

-- 적립취소 테스트용 (미사용 적립건 보유) (8203)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008202', X'00000000000000000000000000008203', 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000082020', X'00000000000000000000000000008202', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);
