-- API 통합 테스트용 데이터 
-- member_id: 8001 ~ 8020

-- 잔액 조회 테스트용
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008001', X'00000000000000000000000000008001', 5000, 5000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000008001', X'00000000000000000000000000008001', 'EARN', 5000, NULL, CURRENT_TIMESTAMP);

-- 적립 테스트용 (신규 회원) - 데이터 없음 (8002)

-- 적립취소 테스트용 (미사용 적립건 보유)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008002', X'00000000000000000000000000008003', 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000008002', X'00000000000000000000000000008002', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

-- 사용 테스트용 (잔액 보유)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008003', X'00000000000000000000000000008004', 3000, 3000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000008003', X'00000000000000000000000000008003', 'EARN', 3000, NULL, CURRENT_TIMESTAMP);

-- 사용취소 테스트용 (사용 내역 있음)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000008004', X'00000000000000000000000000008005', 2000, 0, 2000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000008010', X'00000000000000000000000000008004', 'EARN', 2000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000008011', X'00000000000000000000000000008004', 'USE', -2000, 'ORDER-API-TEST', CURRENT_TIMESTAMP);
