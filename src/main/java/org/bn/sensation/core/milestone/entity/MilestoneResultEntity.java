package org.bn.sensation.core.milestone.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone_result")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResultEntity extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_id")
    private MilestoneEntity milestone;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;

    //TODO не факт что нужно. М.б. убрать
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "round_id")
    private RoundEntity round;

    @Column(name = "judge_approved")
    private Boolean judgeApproved;

    @Column(name = "finally_approved")
    private Boolean finallyApproved;

    @Column(name = "total_score")
    private Integer totalScore;

}
