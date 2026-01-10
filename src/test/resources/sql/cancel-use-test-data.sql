-- 사용취소 테스트용 데이터 (ID 범위: 4000 ~ 4999)
-- member_id: 4001 ~ 4010
-- ledger_id: 4001 ~ 4020
-- transaction_id: 4001 ~ 4030

-- CU-T01: 전액 사용취소
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4001, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4001, 4001, 1000, 0, 1000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4001, 4001, 'EARN', 1000, 4001, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4002, 4001, 'USE', 1000, 'ORDER-CU-T01', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4001, 4002, 4001, 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CU-T02: 부분 사용취소
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4002, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4002, 4002, 1000, 0, 1000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4003, 4002, 'EARN', 1000, 4002, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4004, 4002, 'USE', 1000, 'ORDER-CU-T02', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4002, 4004, 4002, 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CU-T03: 여러 적립건 부분 취소 (A 500 + B 300 사용, 600 취소)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4003, 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4003, 4003, 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4004, 4003, 500, 200, 300, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4005, 4003, 'EARN', 500, 4003, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4006, 4003, 'EARN', 500, 4004, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4007, 4003, 'USE', 800, 'ORDER-CU-T03', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4003, 4007, 4003, 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4004, 4007, 4004, 300, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CU-T04: 만료 안된 적립건 복구
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4004, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4005, 4004, 500, 0, 500, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4008, 4004, 'EARN', 500, 4005, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4009, 4004, 'USE', 500, 'ORDER-CU-T04', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4005, 4009, 4005, 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CU-T05: 만료된 적립건 복구 (신규 적립)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4005, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4006, 4005, 500, 0, 500, 'SYSTEM', DATEADD('DAY', -1, CURRENT_TIMESTAMP), false, DATEADD('DAY', -366, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4010, 4005, 'EARN', 500, 4006, DATEADD('DAY', -366, CURRENT_TIMESTAMP));

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4011, 4005, 'USE', 500, 'ORDER-CU-T05', DATEADD('DAY', -2, CURRENT_TIMESTAMP));

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4006, 4011, 4006, 500, 0, DATEADD('DAY', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- CU-T06: 혼합 (만료+미만료) (A 만료 500, B 미만료 500, 800 사용)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4006, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4007, 4006, 500, 0, 500, 'SYSTEM', DATEADD('DAY', -1, CURRENT_TIMESTAMP), false, DATEADD('DAY', -366, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4008, 4006, 500, 200, 300, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4012, 4006, 'EARN', 500, 4007, DATEADD('DAY', -366, CURRENT_TIMESTAMP));

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4013, 4006, 'EARN', 500, 4008, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4014, 4006, 'USE', 800, 'ORDER-CU-T06', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4007, 4014, 4007, 500, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4008, 4014, 4008, 300, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CU-T07: 취소 가능 금액 초과 (사용 1000, 1500 취소 시도)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4007, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4009, 4007, 1000, 0, 1000, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4015, 4007, 'EARN', 1000, 4009, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4016, 4007, 'USE', 1000, 'ORDER-CU-T07', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4009, 4016, 4009, 1000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CU-T08: 이미 전액 취소된 건 재취소
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (4008, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, created_at, updated_at)
VALUES (4010, 4008, 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, created_at)
VALUES (4017, 4008, 'EARN', 1000, 4010, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, created_at)
VALUES (4018, 4008, 'USE', 1000, 'ORDER-CU-T08', CURRENT_TIMESTAMP);

INSERT INTO point_usage_detail (id, transaction_id, ledger_id, used_amount, canceled_amount, created_at, updated_at)
VALUES (4010, 4018, 4010, 1000, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, order_id, related_transaction_id, created_at)
VALUES (4019, 4008, 'USE_CANCEL', 1000, 'ORDER-CU-T08', 4018, CURRENT_TIMESTAMP);
