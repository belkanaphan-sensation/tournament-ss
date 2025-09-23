package org.bn.sensation.core.user.entity;

import java.time.LocalDateTime;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.PartnerSide;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_activity_assignment")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityAssignmentEntity extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "activity_id", nullable = false)
    private ActivityEntity activity;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private UserActivityPosition position;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_side")
    private PartnerSide partnerSide;

    @Column(name = "assigned_at")
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
}
