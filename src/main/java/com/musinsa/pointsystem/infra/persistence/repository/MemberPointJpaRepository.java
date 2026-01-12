package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemberPointJpaRepository extends JpaRepository<MemberPointEntity, UUID> {
    // MemberPoint 조회는 단순 findById 사용
    // Ledger는 별도 쿼리로 조회하여 N+1 문제 방지 및 DB 레벨 정렬/필터링 보장
}
