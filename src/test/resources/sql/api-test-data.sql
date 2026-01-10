-- API 통합 테스트용 데이터 (ID 범위: 8000 ~ 8999)
-- member_id: 8001 ~ 8020

-- 잔액 조회 테스트용
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8001, 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8001, 8001, 5000, 5000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8001, 8001, 'EARN', 5000, 8001, CURRENT_TIMESTAMP);

-- 적립 테스트용 (신규 회원)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8002, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 적립취소 테스트용 (미사용 적립건 보유)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8003, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8002, 8003, 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8002, 8003, 'EARN', 1000, 8002, CURRENT_TIMESTAMP);

-- 사용 테스트용 (잔액 보유)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8004, 3000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8003, 8004, 3000, 3000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8003, 8004, 'EARN', 3000, 8003, CURRENT_TIMESTAMP);

-- 사용취소 테스트용 (사용 내역 있음)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (8005, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (8004, 8005, 2000, 0, 2000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (8004, 8005, 'EARN', 2000, 8004, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (8005, 8005, 'USE', 2000, 'ORDER-API-TEST', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (8001, 8005, 8004, 2000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
