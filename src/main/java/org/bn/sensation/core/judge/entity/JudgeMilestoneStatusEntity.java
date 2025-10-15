package org.bn.sensation.core.judge.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "judge_milestone_status")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgeMilestoneStatusEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "judge_id")
    private UserActivityAssignmentEntity judge;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    private MilestoneEntity milestone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JudgeMilestoneStatus status;
}
