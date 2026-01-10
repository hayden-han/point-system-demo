-- 사용 테스트용 데이터 (ID 범위: 3000 ~ 3999)
-- member_id: 3001 ~ 3010
-- ledger_id: 3001 ~ 3020
-- transaction_id: 3001 ~ 3020

-- U-T01: 단일 적립건에서 사용
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3001, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3001, 3001, 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3001, 3001, 'EARN', 1000, 3001, CURRENT_TIMESTAMP);

-- U-T02, U-T03: 여러 적립건에서 사용 / 전액 사용
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3002, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3002, 3002, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3003, 3002, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3002, 3002, 'EARN', 500, 3002, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3003, 3002, 'EARN', 500, 3003, CURRENT_TIMESTAMP);

-- U-T04: 수기 지급 우선 사용 (MANUAL 500, SYSTEM 500)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3003, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3004, 3003, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3005, 3003, 500, 500, 0, 'MANUAL', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3004, 3003, 'EARN', 500, 3004, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3005, 3003, 'EARN', 500, 3005, CURRENT_TIMESTAMP);

-- U-T05: 만료일 짧은 순 사용 (만료 10일 500, 만료 30일 500)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3004, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3006, 3004, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 30, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3007, 3004, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 10, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3006, 3004, 'EARN', 500, 3006, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3007, 3004, 'EARN', 500, 3007, CURRENT_TIMESTAMP);

-- U-T06: 수기+만료일 복합 (MANUAL 30일 500, SYSTEM 10일 500 - MANUAL이 우선)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3005, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3008, 3005, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 10, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3009, 3005, 500, 500, 0, 'MANUAL', DATEADD('DAY', 30, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3008, 3005, 'EARN', 500, 3008, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3009, 3005, 'EARN', 500, 3009, CURRENT_TIMESTAMP);

-- U-T07: 잔액 부족 (잔액 500, 사용 1000 시도)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3006, 500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (3010, 3006, 500, 500, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (3010, 3006, 'EARN', 500, 3010, CURRENT_TIMESTAMP);

-- U-T08: 잔액 0 (사용 시도)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (3007, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
