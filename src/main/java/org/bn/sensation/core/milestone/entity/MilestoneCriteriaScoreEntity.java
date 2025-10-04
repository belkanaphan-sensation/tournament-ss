package org.bn.sensation.core.milestone.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone_criteria_score")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneCriteriaScoreEntity extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_result_id")
    private MilestoneResultEntity milestoneResult;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_criteria_assignment_id")
    private MilestoneCriteriaAssignmentEntity milestoneCriteriaAssignment;

    @Column(name = "total_score")
    private Integer totalScore;
}
