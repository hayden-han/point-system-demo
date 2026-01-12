package com.musinsa.pointsystem.infra.persistence.entity;

import com.musinsa.pointsystem.common.util.UuidGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "point_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicyEntity extends BaseEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "policy_key", nullable = false, unique = true, length = 50)
    private String policyKey;

    @Column(name = "policy_value", nullable = false)
    private Long policyValue;

    @Column(name = "description", length = 200)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidGenerator.generate();
        }
    }
}
