package com.musinsa.pointsystem.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "member_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPointEntity {

    @Id
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    private UUID memberId;

    @Column(name = "total_balance", nullable = false)
    private Long totalBalance;

    /**
     * 읽기 전용 관계 - Ledger 저장은 PointLedgerJpaRepository에서 직접 처리
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private List<PointLedgerEntity> ledgers = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MemberPointEntity(UUID memberId, Long totalBalance) {
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
