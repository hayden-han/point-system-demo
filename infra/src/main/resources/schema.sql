-- =============================================================================
-- 포인트 시스템 스키마 
-- =============================================================================
-- 설계 원칙:
--   - 3개 테이블: point_ledger, ledger_entry, point_policy
--   - 논리적 FK: 물리적 FK 제약조건 없이 애플리케이션 레벨에서 참조 무결성 관리
--   - UUID: 분산 시스템 대응을 위해 BINARY(16) 사용
--   - UUIDv7: 애플리케이션에서 시간 기반 UUID 생성 (인덱스 성능 최적화)
--   - DATETIME + UTC: 2038년 문제 회피, 애플리케이션에서 UTC로 변환하여 저장
--   - Single Source of Truth: LedgerEntry가 유일한 변동 기록
--   - totalBalance는 조회 시점에 계산 (만료 실시간 반영)
-- =============================================================================

-- Point Policy (정책 설정)
CREATE TABLE IF NOT EXISTS point_policy (
    id BINARY(16) PRIMARY KEY COMMENT '정책 ID (UUIDv7)',
    policy_key VARCHAR(50) NOT NULL UNIQUE COMMENT '정책 키 (예: MAX_EARN_AMOUNT, DEFAULT_EXPIRY_DAYS)',
    policy_value BIGINT NOT NULL COMMENT '정책 값',
    description VARCHAR(200) COMMENT '정책 설명',
    created_at DATETIME NOT NULL COMMENT '생성일시 (UTC)',
    updated_at DATETIME NOT NULL COMMENT '수정일시 (UTC)'
) COMMENT '포인트 정책 설정 테이블';

-- Point Ledger (적립건)
CREATE TABLE IF NOT EXISTS point_ledger (
    id BINARY(16) PRIMARY KEY COMMENT '적립건 ID (UUIDv7)',
    member_id BINARY(16) NOT NULL COMMENT '회원 ID (논리적 FK → member 테이블)',
    earned_amount BIGINT NOT NULL COMMENT '최초 적립 금액',
    available_amount BIGINT NOT NULL COMMENT '현재 사용 가능 금액',
    used_amount BIGINT NOT NULL DEFAULT 0 COMMENT '누적 사용 금액',
    earn_type VARCHAR(20) NOT NULL COMMENT '적립 유형 (ORDER_EARN, REVIEW_EARN, EVENT_EARN, USE_CANCEL)',
    source_ledger_id BINARY(16) COMMENT '원본 적립건 ID (논리적 FK → point_ledger.id, USE_CANCEL 시 참조)',
    expired_at DATETIME NOT NULL COMMENT '만료일시 (UTC)',
    is_canceled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '적립 취소 여부',
    earned_at DATETIME NOT NULL COMMENT '적립일시 (UTC)',
    created_at DATETIME NOT NULL COMMENT '생성일시 (UTC)',
    updated_at DATETIME NOT NULL COMMENT '수정일시 (UTC)',
    CONSTRAINT chk_earned_positive CHECK (earned_amount > 0),
    CONSTRAINT chk_available_range CHECK (available_amount >= 0 AND available_amount <= earned_amount)
) COMMENT '포인트 적립건 테이블 (적립 단위 관리)';

CREATE INDEX IF NOT EXISTS idx_ledger_member_expired ON point_ledger (member_id, expired_at);
CREATE INDEX IF NOT EXISTS idx_ledger_source ON point_ledger (source_ledger_id);

-- Ledger Entry (적립건 변동 이력)
CREATE TABLE IF NOT EXISTS ledger_entry (
    id BINARY(16) PRIMARY KEY COMMENT '변동 이력 ID (UUIDv7)',
    ledger_id BINARY(16) NOT NULL COMMENT '적립건 ID (논리적 FK → point_ledger.id)',
    type VARCHAR(20) NOT NULL COMMENT '변동 유형 (EARN, EARN_CANCEL, USE, USE_CANCEL)',
    amount BIGINT NOT NULL COMMENT '변동 금액 (+: 적립/복구, -: 사용/취소)',
    order_id VARCHAR(100) COMMENT '주문 ID (논리적 FK → order 테이블)',
    created_at DATETIME NOT NULL COMMENT '생성일시 (UTC)',
    CONSTRAINT chk_type_valid CHECK (type IN ('EARN', 'EARN_CANCEL', 'USE', 'USE_CANCEL'))
) COMMENT '적립건 변동 이력 테이블 (Single Source of Truth)';

CREATE INDEX IF NOT EXISTS idx_entry_ledger ON ledger_entry (ledger_id, created_at);
CREATE INDEX IF NOT EXISTS idx_entry_order ON ledger_entry (order_id);

-- =============================================================================
-- Batch 정합성 검증 결과 테이블
-- =============================================================================

CREATE TABLE IF NOT EXISTS consistency_check_result (
    ledger_id BINARY(16) PRIMARY KEY COMMENT '적립건 ID (논리적 FK → point_ledger.id)',
    member_id BINARY(16) NOT NULL COMMENT '회원 ID (논리적 FK → member 테이블)',
    inconsistency_type VARCHAR(50) NOT NULL COMMENT '불일치 유형 (BALANCE_MISMATCH, NEGATIVE_AVAILABLE 등)',
    details VARCHAR(500) COMMENT '불일치 상세 내용 (JSON 형식)',
    detected_at DATETIME NOT NULL COMMENT '발견일시 (UTC)',
    resolved_at DATETIME COMMENT '해결일시 (UTC)',
    resolution_note VARCHAR(500) COMMENT '해결 내용'
) COMMENT '배치 정합성 검증 결과 테이블';

CREATE INDEX IF NOT EXISTS idx_consistency_member ON consistency_check_result (member_id);
CREATE INDEX IF NOT EXISTS idx_consistency_type ON consistency_check_result (inconsistency_type, detected_at);
