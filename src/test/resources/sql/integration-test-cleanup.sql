-- 통합 테스트 데이터 정리 (ID 범위: 6000 ~ 6999)
DELETE FROM point_usage_detail WHERE id BETWEEN 6000 AND 6999;
DELETE FROM point_transaction WHERE id BETWEEN 6000 AND 6999;
DELETE FROM point_ledger WHERE id BETWEEN 6000 AND 6999;
DELETE FROM member_point WHERE member_id BETWEEN 6000 AND 6999;
