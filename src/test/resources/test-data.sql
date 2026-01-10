-- 테스트용 초기 정책 데이터 (MERGE 사용하여 중복 방지)
MERGE INTO point_policy (policy_key, policy_value, description) KEY(policy_key) VALUES
('EARN_MIN_AMOUNT', 1, '1회 최소 적립 금액'),
('EARN_MAX_AMOUNT', 100000, '1회 최대 적립 금액'),
('BALANCE_MAX_AMOUNT', 10000000, '개인별 최대 보유 가능 금액'),
('EXPIRATION_DEFAULT_DAYS', 365, '기본 만료일 (일)'),
('EXPIRATION_MIN_DAYS', 1, '최소 만료일 (일)'),
('EXPIRATION_MAX_DAYS', 1824, '최대 만료일 (일, 5년 미만)');
