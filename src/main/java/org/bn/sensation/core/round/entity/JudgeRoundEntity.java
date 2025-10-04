package org.bn.sensation.core.round.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "judge_round_status")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgeRoundEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "judge_id")
    private UserActivityAssignmentEntity judge;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private RoundEntity round;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JudgeRoundStatus status;
}
