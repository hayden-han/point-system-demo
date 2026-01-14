-- 정합성 검증 테스트용 데이터 (v2 스키마)
-- member_id: 7001 ~ 7010

-- V-T01: 잔액 정합성 (SUM(valid ledger.available_amount) == 총 잔액)
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

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070010', X'00000000000000000000000000007001', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070011', X'00000000000000000000000000007001', 'USE', -200, 'ORDER-V-T01', CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070012', X'00000000000000000000000000007002', 'EARN', 700, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070013', X'00000000000000000000000000007003', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070014', X'00000000000000000000000000007004', 'EARN', 300, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070015', X'00000000000000000000000000007004', 'EARN_CANCEL', -300, NULL, CURRENT_TIMESTAMP);

-- V-T02: 적립건 정합성 (earned_amount == available_amount + used_amount)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007005', X'00000000000000000000000000007002', 1000, 500, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070020', X'00000000000000000000000000007005', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070021', X'00000000000000000000000000007005', 'USE', -500, 'ORDER-V-T02', CURRENT_TIMESTAMP);

-- V-T03: 사용상세 정합성 (v2: Entry 기반 - 같은 orderId의 USE Entry 합계 == 사용 금액)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007006', X'00000000000000000000000000007003', 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007007', X'00000000000000000000000000007003', 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070030', X'00000000000000000000000000007006', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070031', X'00000000000000000000000000007007', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070032', X'00000000000000000000000000007006', 'USE', -500, 'ORDER-V-T03', CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070033', X'00000000000000000000000000007007', 'USE', -500, 'ORDER-V-T03', CURRENT_TIMESTAMP);

-- V-T04: 취소 정합성 (v2: Entry 기반 - USE_CANCEL 합계 <= USE 합계)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000007008', X'00000000000000000000000000007004', 1000, 300, 700, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070040', X'00000000000000000000000000007008', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070041', X'00000000000000000000000000007008', 'USE', -1000, 'ORDER-V-T04', CURRENT_TIMESTAMP);

-- 1000 사용 중 300 취소됨
INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000070042', X'00000000000000000000000000007008', 'USE_CANCEL', 300, 'ORDER-V-T04', CURRENT_TIMESTAMP);
