package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPointEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "total_balance", nullable = false)
    private Long totalBalance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MemberPointEntity(Long memberId, Long totalBalance) {
        this.memberId = memberId;
        this.totalBalance = totalBalance;
    }

    public void updateTotalBalance(Long totalBalance) {
        this.totalBalance = totalBalance;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
