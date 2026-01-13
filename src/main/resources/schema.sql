-- =============================================================================
-- 포인트 시스템 스키마
-- =============================================================================
-- 설계 원칙:
--   - 논리적 FK: 물리적 FK 제약조건 없이 애플리케이션 레벨에서 참조 무결성 관리
--   - 이유: 스키마 변경 유연성 확보, 대용량 데이터 처리 시 성능 최적화
--   - 테이블 간 관계는 인덱스로 조회 성능 보장
--   - UUID: 분산 시스템 대응을 위해 BINARY(16) 사용
--   - UUIDv7: 애플리케이션에서 시간 기반 UUID 생성 (인덱스 성능 최적화)
--   - DATETIME + UTC: 2038년 문제 회피, 애플리케이션에서 UTC로 변환하여 저장
-- =============================================================================

-- Point Policy (정책 설정)
CREATE TABLE IF NOT EXISTS point_policy (
    id BINARY(16) PRIMARY KEY,
    policy_key VARCHAR(50) NOT NULL UNIQUE,
    policy_value BIGINT NOT NULL,
    description VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Member Point (회원 포인트 잔액)
CREATE TABLE IF NOT EXISTS member_point (
    member_id BINARY(16) PRIMARY KEY,
    total_balance BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT chk_balance_positive CHECK (total_balance >= 0)
);

-- Point Ledger (적립 원장)
CREATE TABLE IF NOT EXISTS point_ledger (
    id BINARY(16) PRIMARY KEY,
    member_id BINARY(16) NOT NULL,
    earned_amount BIGINT NOT NULL,
    available_amount BIGINT NOT NULL,
    used_amount BIGINT NOT NULL DEFAULT 0,
    earn_type VARCHAR(20) NOT NULL,
    source_transaction_id BINARY(16),
    expired_at DATETIME NOT NULL,
    is_canceled BOOLEAN NOT NULL DEFAULT FALSE,
    earned_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT chk_earned_positive CHECK (earned_amount > 0),
    CONSTRAINT chk_available_range CHECK (available_amount >= 0 AND available_amount <= earned_amount)
);

CREATE INDEX IF NOT EXISTS idx_ledger_member_available ON point_ledger (member_id, is_canceled, expired_at, earn_type, available_amount);
CREATE INDEX IF NOT EXISTS idx_ledger_source_tx ON point_ledger (source_transaction_id);

-- Ledger Entry (적립건 변동 이력) - v2
CREATE TABLE IF NOT EXISTS ledger_entry (
    id BINARY(16) PRIMARY KEY,
    ledger_id BINARY(16) NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    order_id VARCHAR(100),
    created_at DATETIME NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_entry_ledger ON ledger_entry (ledger_id, created_at);
CREATE INDEX IF NOT EXISTS idx_entry_order ON ledger_entry (order_id);

-- Point Transaction (포인트 트랜잭션)
CREATE TABLE IF NOT EXISTS point_transaction (
    id BINARY(16) PRIMARY KEY,
    member_id BINARY(16) NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    order_id VARCHAR(100),
    related_transaction_id BINARY(16),
    ledger_id BINARY(16),
    transacted_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transaction_member ON point_transaction (member_id, created_at);
CREATE INDEX IF NOT EXISTS idx_transaction_order ON point_transaction (order_id);
CREATE INDEX IF NOT EXISTS idx_transaction_related ON point_transaction (related_transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_ledger ON point_transaction (ledger_id);

-- Point Usage Detail (사용 상세)
CREATE TABLE IF NOT EXISTS point_usage_detail (
    id BINARY(16) PRIMARY KEY,
    transaction_id BINARY(16) NOT NULL,
    ledger_id BINARY(16) NOT NULL,
    used_amount BIGINT NOT NULL,
    canceled_amount BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT chk_canceled_range CHECK (canceled_amount >= 0 AND canceled_amount <= used_amount)
);

CREATE INDEX IF NOT EXISTS idx_usage_transaction ON point_usage_detail (transaction_id);
CREATE INDEX IF NOT EXISTS idx_usage_ledger ON point_usage_detail (ledger_id);
