-- 적립취소 테스트용 데이터 (ID 범위: 2000 ~ 2999)
-- member_id: 2001 ~ 2010
-- ledger_id: 2001 ~ 2020
-- transaction_id: 2001 ~ 2020

-- CE-T01: 미사용 적립건 (취소 가능)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (2001, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (2001, 2001, 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (2001, 2001, 'EARN', 1000, 2001, CURRENT_TIMESTAMP);

-- CE-T02: 일부 사용된 적립건
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (2002, 500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (2002, 2002, 1000, 500, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (2002, 2002, 'EARN', 1000, 2002, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (2003, 2002, 'USE', 500, 'ORDER-CE-T02', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (2001, 2003, 2002, 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CE-T03: 전액 사용된 적립건
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (2003, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (2003, 2003, 1000, 0, 1000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (2004, 2003, 'EARN', 1000, 2003, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (2005, 2003, 'USE', 1000, 'ORDER-CE-T03', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (2002, 2005, 2003, 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CE-T04: 이미 취소된 적립건
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (2004, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (2004, 2004, 1000, 0, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (2006, 2004, 'EARN', 1000, 2004, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (2007, 2004, 'EARN_CANCEL', 1000, 2004, CURRENT_TIMESTAMP);
