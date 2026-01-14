-- 적립취소 테스트용 데이터 (v2 스키마)
-- member_id: 2001 ~ 2010
-- ledger_id: 2001 ~ 2020
-- entry_id: 20010 ~ 20100

-- CE-T01: 미사용 적립건 (취소 가능)
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000002001', X'00000000000000000000000000002001', 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020010', X'00000000000000000000000000002001', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

-- CE-T02: 일부 사용된 적립건
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000002002', X'00000000000000000000000000002002', 1000, 500, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020020', X'00000000000000000000000000002002', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020021', X'00000000000000000000000000002002', 'USE', -500, 'ORDER-CE-T02', CURRENT_TIMESTAMP);

-- CE-T03: 전액 사용된 적립건
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000002003', X'00000000000000000000000000002003', 1000, 0, 1000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020030', X'00000000000000000000000000002003', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020031', X'00000000000000000000000000002003', 'USE', -1000, 'ORDER-CE-T03', CURRENT_TIMESTAMP);

-- CE-T04: 이미 취소된 적립건
INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000002004', X'00000000000000000000000000002004', 1000, 0, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020040', X'00000000000000000000000000002004', 'EARN', 1000, NULL, CURRENT_TIMESTAMP);

INSERT INTO ledger_entry (id, ledger_id, type, amount, order_id, created_at)
VALUES (X'00000000000000000000000000020041', X'00000000000000000000000000002004', 'EARN_CANCEL', -1000, NULL, CURRENT_TIMESTAMP);
