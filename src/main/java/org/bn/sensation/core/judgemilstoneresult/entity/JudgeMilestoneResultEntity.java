package org.bn.sensation.core.judgemilstoneresult.entity;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;

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

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "is_candidate")
    private Boolean isCandidate;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private RoundEntity round;

    @ManyToOne
    @JoinColumn(name = "milestone_criterion_id")
    private MilestoneCriterionEntity milestoneCriterion;

    @ManyToOne
    @JoinColumn(name = "activity_user_id")
    private ActivityUserEntity activityUser;

}
