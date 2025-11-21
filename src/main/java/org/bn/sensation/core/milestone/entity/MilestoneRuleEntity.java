package org.bn.sensation.core.milestone.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.contestant.entity.ContestantType;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone_rule")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MilestoneRuleEntity extends BaseEntity {

    @Column(name = "assessment_mode", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssessmentMode assessmentMode;

    @Column(name = "contestant_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestantType contestantType;

    //TODO вероятно в будущем заменится на какой-то объект
    @Column(name = "contestant_limit", nullable = false)
    private Integer contestantLimit;

    @Column(name = "round_contestant_limit", nullable = false)
    private Integer roundContestantLimit;

    @Column(name = "strict_pass_mode", nullable = false)
    private Boolean strictPassMode;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_id", unique = true)
    private MilestoneEntity milestone;

    @OneToMany(mappedBy = "milestoneRule",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @Builder.Default
    private Set<MilestoneCriterionEntity> milestoneCriteria = new HashSet<>();
}
