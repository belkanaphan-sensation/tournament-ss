package org.bn.sensation.core.milestone.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "judge_milestone_result")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgeMilestoneResultEntity extends BaseEntity {

    @Column(name = "score")
    private Integer score;

    @Column(name = "favorite")
    private Boolean isFavorite;

    @Column(name = "candidate")
    private Boolean isCandidate;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private RoundEntity round;

    @ManyToOne
    @JoinColumn(name = "milestone_criteria_id")
    private MilestoneCriteriaAssignmentEntity milestoneCriteria;

    @ManyToOne
    @JoinColumn(name = "activity_user_id")
    private UserActivityAssignmentEntity activityUser;

}
