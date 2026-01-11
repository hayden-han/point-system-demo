package com.musinsa.pointsystem.presentation.dto.response;

import com.musinsa.pointsystem.domain.model.MemberPoint;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PointBalanceResponse {
    private final UUID memberId;
    private final Long totalBalance;

    public static PointBalanceResponse from(MemberPoint memberPoint) {
        return PointBalanceResponse.builder()
                .memberId(memberPoint.getMemberId())
                .totalBalance(memberPoint.getTotalBalance())
                .build();
    }
}
