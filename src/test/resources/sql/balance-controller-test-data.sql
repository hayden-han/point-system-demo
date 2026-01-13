-- PointBalanceController 테스트용 데이터 (UUID 기반) - v2 구조

-- 잔액 조회 테스트용 (8101)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000008101', 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008101', X'00000000000000000000000000008101', 5000, 5000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000081010', X'00000000000000000000000000008101', 'EARN', 5000, NULL, CURRENT_TIMESTAMP);

-- 거래 내역 테스트용 point_transaction (레거시 호환)
INSERT INTO point_transaction (id, member_id, type, amount, order_id, related_transaction_id, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000008110', X'00000000000000000000000000008101', 'EARN', 5000, NULL, NULL, X'00000000000000000000000000008101', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 잔액 0인 회원 (8102)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000008102', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용 내역이 있는 회원 (8105)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000008105', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008105', X'00000000000000000000000000008105', 2000, 0, 2000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000081050', X'00000000000000000000000000008105', 'EARN', 2000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000081051', X'00000000000000000000000000008105', 'USE', -2000, 'ORDER-BALANCE-TEST', CURRENT_TIMESTAMP);

-- 거래 내역 테스트용 point_transaction (레거시 호환)
INSERT INTO point_transaction (id, member_id, type, amount, order_id, related_transaction_id, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000008150', X'00000000000000000000000000008105', 'EARN', 2000, NULL, NULL, X'00000000000000000000000000008105', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, related_transaction_id, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000008151', X'00000000000000000000000000008105', 'USE', 2000, 'ORDER-BALANCE-TEST', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
