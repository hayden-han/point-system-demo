-- 동시성 테스트용 데이터 (UUID 기반)
-- member_id: 5001 ~ 5010

-- C-T01: 동시 적립 정합성 테스트 (초기 잔액 0)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000005001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- C-T02: 동시 사용 정합성 테스트 (초기 잔액 10000)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000005002', 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000005001', X'00000000000000000000000000005002', 10000, 10000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000005001', X'00000000000000000000000000005002', 'EARN', 10000, X'00000000000000000000000000005001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- C-T03: 적립+사용 동시 정합성 테스트 (초기 잔액 5000)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000005003', 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000005002', X'00000000000000000000000000005003', 5000, 5000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000005002', X'00000000000000000000000000005003', 'EARN', 5000, X'00000000000000000000000000005002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- C-T04, C-T05: 락 획득 재시도/최종 실패 테스트 (초기 잔액 1000)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000005004', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_ledger (id, member_id, earned_amount, available_amount, used_amount, earn_type, expired_at, is_canceled, earned_at, created_at, updated_at)
VALUES (X'00000000000000000000000000005003', X'00000000000000000000000000005004', 1000, 1000, 0, 'SYSTEM', DATEADD('DAY', 365, CURRENT_TIMESTAMP), false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO point_transaction (id, member_id, type, amount, ledger_id, transacted_at, created_at)
VALUES (X'00000000000000000000000000005003', X'00000000000000000000000000005004', 'EARN', 1000, X'00000000000000000000000000005003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
