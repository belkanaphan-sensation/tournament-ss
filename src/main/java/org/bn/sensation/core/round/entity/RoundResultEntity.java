package org.bn.sensation.core.round.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "round_result")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoundResultEntity extends BaseEntity {

    @Column(name = "score")
    private Integer score;

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
