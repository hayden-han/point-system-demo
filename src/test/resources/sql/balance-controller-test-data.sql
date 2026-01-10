-- PointBalanceController 테스트용 데이터 (ID 범위: 8100 ~ 8199)

-- 잔액 조회 테스트용 (8101)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8101, 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8101, 8101, 5000, 5000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8101, 8101, 'EARN', 5000, 8101, CURRENT_TIMESTAMP);

-- 잔액 0인 회원 (8102)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8102, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용 내역이 있는 회원 (8105)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8105, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8105, 8105, 2000, 0, 2000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8105, 8105, 'EARN', 2000, 8105, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (8106, 8105, 'USE', 2000, 'ORDER-BALANCE-TEST', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (8105, 8106, 8105, 2000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
