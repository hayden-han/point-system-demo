-- 동시성 테스트 데이터 정리 (ID 범위: 5000 ~ 5499)
DELETE FROM point_usage_detail WHERE id BETWEEN 5000 AND 5499;
DELETE FROM point_transaction WHERE id BETWEEN 5000 AND 5499;
DELETE FROM point_ledger WHERE id BETWEEN 5000 AND 5499;
DELETE FROM member_point WHERE member_id BETWEEN 5000 AND 5499;
