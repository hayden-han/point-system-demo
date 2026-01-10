-- PointEarnController 테스트 데이터 정리 (ID 범위: 8200 ~ 8299)
DELETE FROM point_usage_detail WHERE id BETWEEN 8200 AND 8299;
DELETE FROM point_transaction WHERE id BETWEEN 8200 AND 8299;
DELETE FROM point_ledger WHERE id BETWEEN 8200 AND 8299;
DELETE FROM member_point WHERE member_id BETWEEN 8200 AND 8299;
