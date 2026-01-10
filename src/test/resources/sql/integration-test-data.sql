-- 통합 테스트용 데이터 (ID 범위: 6000 ~ 6999)
-- member_id: 6001 ~ 6010
-- INT-T01: 요구사항 예시 전체 흐름
-- 이 테스트는 빈 상태에서 시작하여 전체 흐름을 검증
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (6001, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
