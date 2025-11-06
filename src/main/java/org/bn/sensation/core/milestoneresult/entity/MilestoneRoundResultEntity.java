package org.bn.sensation.core.milestoneresult.entity;

import java.math.BigDecimal;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.round.entity.RoundEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone_round_result")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneRoundResultEntity extends BaseEntity {

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Column(name = "judge_passed")
    private PassStatus judgePassed;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "round_id")
    private RoundEntity round;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_result_id")
    private MilestoneResultEntity milestoneResult;
}
