-- 사용 테스트 데이터 정리 (ID 범위: 3000 ~ 3999)
DELETE FROM point_usage_detail WHERE id BETWEEN 3000 AND 3999;
DELETE FROM point_transaction WHERE id BETWEEN 3000 AND 3999;
DELETE FROM point_ledger WHERE id BETWEEN 3000 AND 3999;
DELETE FROM member_point WHERE member_id BETWEEN 3000 AND 3999;
