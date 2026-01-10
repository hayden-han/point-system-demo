-- PointUseController 테스트용 데이터 (ID 범위: 8300 ~ 8399)

-- 사용 테스트용 (잔액 보유) (8304)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8304, 3000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8303, 8304, 3000, 3000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8303, 8304, 'EARN', 3000, 8303, CURRENT_TIMESTAMP);

-- 사용취소 테스트용 (사용 내역 있음) (8305)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8305, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8304, 8305, 2000, 0, 2000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8304, 8305, 'EARN', 2000, 8304, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (8305, 8305, 'USE', 2000, 'ORDER-USE-CTRL-TEST', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (8301, 8305, 8304, 2000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
