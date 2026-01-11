-- 적립 테스트용 데이터 (UUID 기반)
-- member_id: 1001 ~ 1010

-- 최대 보유금액 테스트용 회원 (E-T08)
INSERT INTO member_point (member_id, total_balance, created_at, updated_at)
VALUES (X'00000000000000000000000000001001', 9500000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
