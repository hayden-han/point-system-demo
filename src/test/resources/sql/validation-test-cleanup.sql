-- 정합성 검증 테스트 데이터 정리 (ID 범위: 7000 ~ 7999)
DELETE FROM point_usage_detail WHERE id BETWEEN 7000 AND 7999;
DELETE FROM point_transaction WHERE id BETWEEN 7000 AND 7999;
DELETE FROM point_ledger WHERE id BETWEEN 7000 AND 7999;
DELETE FROM member_point WHERE member_id BETWEEN 7000 AND 7999;
