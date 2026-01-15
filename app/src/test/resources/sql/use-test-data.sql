-- 사용 테스트용 데이터 
-- member_id: 3001 ~ 3010
-- ledger_id: 3001 ~ 3020
-- entry_id: 30010 ~ 30100

-- U-T01: 단일 적립건에서 사용
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003001', X'00000000000000000000000000003001', 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030010', X'00000000000000000000000000003001', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

-- U-T02, U-T03: 여러 적립건에서 사용 / 전액 사용
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003002', X'00000000000000000000000000003002', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003003', X'00000000000000000000000000003002', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030020', X'00000000000000000000000000003002', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030021', X'00000000000000000000000000003003', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

-- U-T04: 수기 지급 우선 사용 (MANUAL 500, SYSTEM 500)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003004', X'00000000000000000000000000003003', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003005', X'00000000000000000000000000003003', 500, 500, 0, 'MANUAL', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030030', X'00000000000000000000000000003004', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030031', X'00000000000000000000000000003005', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

-- U-T05: 만료일 짧은 순 사용 (만료 10일 500, 만료 30일 500)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003006', X'00000000000000000000000000003004', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 30, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003007', X'00000000000000000000000000003004', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 10, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030040', X'00000000000000000000000000003006', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030041', X'00000000000000000000000000003007', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

-- U-T06: 수기+만료일 복합 (MANUAL 30일 500, SYSTEM 10일 500 - MANUAL이 우선)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003008', X'00000000000000000000000000003005', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 10, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003009', X'00000000000000000000000000003005', 500, 500, 0, 'MANUAL', DATEADD('DAY', 30, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030050', X'00000000000000000000000000003008', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030051', X'00000000000000000000000000003009', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

-- U-T07: 잔액 부족 (잔액 500, 사용 1000 시도)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000003010', X'00000000000000000000000000003006', 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000030060', X'00000000000000000000000000003010', 'EARN', 500, NULL, CURRENT_TIMESTAMP);

-- U-T08: 잔액 0 (사용 시도) - 데이터 없음 (3007)
