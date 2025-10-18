package org.bn.sensation.core.useractivity.entity;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.user.entity.UserEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_activity_assignment")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityAssignmentEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private ActivityEntity activity;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private UserActivityPosition position;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_side")
    private PartnerSide partnerSide;

}
