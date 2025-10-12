package org.bn.sensation.core.milestone.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;

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

    //TODO вероятно в будущем заменится на какой-то объект
    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit;

    @Column(name = "round_participant_limit", nullable = false)
    private Integer roundParticipantLimit;

    @Column(name = "strict_pass_mode")
    private Boolean strictPassMode;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_id", unique = true)
    private MilestoneEntity milestone;

    @OneToMany(mappedBy = "milestoneRule",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @Builder.Default
    private Set<MilestoneCriteriaAssignmentEntity> criteriaAssignments = new HashSet<>();
}
