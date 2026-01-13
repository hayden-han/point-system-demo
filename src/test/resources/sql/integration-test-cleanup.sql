-- 통합 테스트 데이터 정리 (UUID 기반)
DELETE FROM ledger_entry;
-- IntegrationScenarioTest는 동적 UUID를 생성하므로 전체 테이블 정리 필요
TRUNCATE TABLE point_transaction;
TRUNCATE TABLE point_ledger;
TRUNCATE TABLE member_point;
