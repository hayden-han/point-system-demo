-- 정합성 검증 테스트용 데이터 (UUID 기반)
-- member_id: 7001 ~ 7010

-- V-T01: 잔액 정합성 (member_point.total_balance == SUM(valid ledger.available_amount))
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000007001', 1500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 유효한 적립건 2개 (합계 1500)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007001', X'00000000000000000000000000007001', 1000, 800, 200, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007002', X'00000000000000000000000000007001', 700, 700, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 만료된 적립건 (잔액 계산에 포함 안됨)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007003', X'00000000000000000000000000007001', 500, 500, 0, 'SYSTEM', DATEADD('DAY', -1, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 취소된 적립건 (잔액 계산에 포함 안됨)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007004', X'00000000000000000000000000007001', 300, 0, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007001', X'00000000000000000000000000007001', 'EARN', 1000, X'00000000000000000000000000007001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007002', X'00000000000000000000000000007001', 'EARN', 700, X'00000000000000000000000000007002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- V-T02: 적립건 정합성 (earned_amount == available_amount + used_amount)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000007002', 500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007005', X'00000000000000000000000000007002', 1000, 500, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007003', X'00000000000000000000000000007002', 'EARN', 1000, X'00000000000000000000000000007005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- V-T03: 사용상세 정합성 (SUM(usage_detail.used_amount) == transaction.amount)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000007003', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007006', X'00000000000000000000000000007003', 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007007', X'00000000000000000000000000007003', 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007004', X'00000000000000000000000000007003', 'EARN', 500, X'00000000000000000000000000007006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007005', X'00000000000000000000000000007003', 'EARN', 500, X'00000000000000000000000000007007', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007006', X'00000000000000000000000000007003', 'USE', 1000, 'ORDER-V-T03', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (X'00000000000000000000000000007001', X'00000000000000000000000000007006', X'00000000000000000000000000007006', 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (X'00000000000000000000000000007002', X'00000000000000000000000000007006', X'00000000000000000000000000007007', 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- V-T04: 취소 정합성 (canceled_amount <= used_amount)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000007004', 300, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007008', X'00000000000000000000000000007004', 1000, 300, 700, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007007', X'00000000000000000000000000007004', 'EARN', 1000, X'00000000000000000000000000007008', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007008', X'00000000000000000000000000007004', 'USE', 1000, 'ORDER-V-T04', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 1000 사용 중 300 취소됨
INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (X'00000000000000000000000000007003', X'00000000000000000000000000007008', X'00000000000000000000000000007008', 1000, 300, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, related_transaction_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000007009', X'00000000000000000000000000007004', 'USE_CANCEL', 300, 'ORDER-V-T04', X'00000000000000000000000000007008', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
