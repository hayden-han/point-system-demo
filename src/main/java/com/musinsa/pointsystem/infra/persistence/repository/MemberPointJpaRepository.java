package com.musinsa.pointsystem.infra.persistence.repository;

import com.musinsa.pointsystem.infra.persistence.entity.MemberPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPointJpaRepository extends JpaRepository<MemberPointEntity, Long> {
}
