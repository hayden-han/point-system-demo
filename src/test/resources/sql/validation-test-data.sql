-- 정합성 검증 테스트용 데이터 (ID 범위: 7000 ~ 7999)
-- member_id: 7001 ~ 7010
-- ledger_id: 7001 ~ 7050
-- transaction_id: 7001 ~ 7050
-- usage_detail_id: 7001 ~ 7050

-- V-T01: 잔액 정합성 (member_point.total_balance == SUM(valid ledger.available_amount))
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (7001, 1500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 유효한 적립건 2개 (합계 1500)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7001, 7001, 1000, 800, 200, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7002, 7001, 700, 700, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 만료된 적립건 (잔액 계산에 포함 안됨)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7003, 7001, 500, 500, 0, 'SYSTEM', DATEADD('DAY', -1, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 취소된 적립건 (잔액 계산에 포함 안됨)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7004, 7001, 300, 0, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (7001, 7001, 'EARN', 1000, 7001, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (7002, 7001, 'EARN', 700, 7002, CURRENT_TIMESTAMP);

-- V-T02: 적립건 정합성 (earned_amount == available_amount + used_amount)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (7002, 500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7005, 7002, 1000, 500, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (7003, 7002, 'EARN', 1000, 7005, CURRENT_TIMESTAMP);

-- V-T03: 사용상세 정합성 (SUM(usage_detail.used_amount) == transaction.amount)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (7003, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7006, 7003, 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7007, 7003, 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (7004, 7003, 'EARN', 500, 7006, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (7005, 7003, 'EARN', 500, 7007, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (7006, 7003, 'USE', 1000, 'ORDER-V-T03', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (7001, 7006, 7006, 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (7002, 7006, 7007, 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- V-T04: 취소 정합성 (canceled_amount <= used_amount)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (7004, 300, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (7008, 7004, 1000, 300, 700, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (7007, 7004, 'EARN', 1000, 7008, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (7008, 7004, 'USE', 1000, 'ORDER-V-T04', CURRENT_TIMESTAMP);

-- 1000 사용 중 300 취소됨
INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (7003, 7008, 7008, 1000, 300, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, related_transaction_id, created_at)
VALUES (7009, 7004, 'USE_CANCEL', 300, 'ORDER-V-T04', 7008, CURRENT_TIMESTAMP);
