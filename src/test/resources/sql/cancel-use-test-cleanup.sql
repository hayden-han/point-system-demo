-- 사용취소 테스트 데이터 정리 (ID 범위: 4000 ~ 4999)
DELETE FROM point_usage_detail WHERE id BETWEEN 4000 AND 4999;
DELETE FROM point_transaction WHERE id BETWEEN 4000 AND 4999;
DELETE FROM point_ledger WHERE id BETWEEN 4000 AND 4999;
DELETE FROM member_point WHERE member_id BETWEEN 4000 AND 4999;
