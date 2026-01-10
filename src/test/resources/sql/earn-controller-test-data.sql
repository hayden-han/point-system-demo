-- PointEarnController 테스트용 데이터 (ID 범위: 8200 ~ 8299)

-- 적립 테스트용 신규 회원 (8202)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8202, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 적립취소 테스트용 (미사용 적립건 보유) (8203)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8203, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8202, 8203, 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8202, 8203, 'EARN', 1000, 8202, CURRENT_TIMESTAMP);
