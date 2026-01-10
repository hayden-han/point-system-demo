package com.musinsa.pointsystem.domain.repository;

import com.musinsa.pointsystem.domain.model.MemberPoint;

import java.util.Optional;

public interface MemberPointRepository {
    Optional<MemberPoint> findByMemberId(Long memberId);
    MemberPoint save(MemberPoint memberPoint);
    MemberPoint getOrCreate(Long memberId);
}
