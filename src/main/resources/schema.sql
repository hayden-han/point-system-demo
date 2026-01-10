-- Point Policy (정책 설정)
CREATE TABLE point_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_key VARCHAR(50) NOT NULL UNIQUE,
    policy_value BIGINT NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Member Point (회원 포인트 잔액)
CREATE TABLE member_point (
    member_id BIGINT PRIMARY KEY,
    total_balance BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_balance_positive CHECK (total_balance >= 0)
);

-- Point Ledger (적립 원장)
CREATE TABLE point_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    earned_amount BIGINT NOT NULL,
    available_amount BIGINT NOT NULL,
    used_amount BIGINT NOT NULL DEFAULT 0,
    earn_type VARCHAR(20) NOT NULL,
    source_transaction_id BIGINT,
    expired_at TIMESTAMP NOT NULL,
    is_canceled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_earned_positive CHECK (earned_amount > 0),
    CONSTRAINT chk_available_range CHECK (available_amount >= 0 AND available_amount <= earned_amount)
);

CREATE INDEX idx_ledger_member_available ON point_ledger (member_id, is_canceled, expired_at, earn_type, available_amount);
CREATE INDEX idx_ledger_source_tx ON point_ledger (source_transaction_id);

-- Point Transaction (포인트 트랜잭션)
CREATE TABLE point_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    order_id VARCHAR(100),
    related_transaction_id BIGINT,
    ledger_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transaction_member ON point_transaction (member_id, created_at);
CREATE INDEX idx_transaction_order ON point_transaction (order_id);
CREATE INDEX idx_transaction_related ON point_transaction (related_transaction_id);

-- Point Usage Detail (사용 상세)
CREATE TABLE point_usage_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    ledger_id BIGINT NOT NULL,
    used_amount BIGINT NOT NULL,
    canceled_amount BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_canceled_range CHECK (canceled_amount >= 0 AND canceled_amount <= used_amount)
);

CREATE INDEX idx_usage_transaction ON point_usage_detail (transaction_id);
CREATE INDEX idx_usage_ledger ON point_usage_detail (ledger_id);
