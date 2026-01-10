-- PointBalanceController 테스트 데이터 정리 (ID 범위: 8100 ~ 8199)
DELETE FROM point_usage_detail WHERE id BETWEEN 8100 AND 8199;
DELETE FROM point_transaction WHERE id BETWEEN 8100 AND 8199;
DELETE FROM point_ledger WHERE id BETWEEN 8100 AND 8199;
DELETE FROM member_point WHERE member_id BETWEEN 8100 AND 8199;
