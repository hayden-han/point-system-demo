package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.util.Optional;
import java.util.UUID;

public interface MemberPointRepository {
    Optional<MemberPoint> findByMemberId(UUID memberId);
    MemberPoint save(MemberPoint memberPoint);
    MemberPoint getOrCreate(UUID memberId);
}
