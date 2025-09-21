package org.bn.sensation.core.milestone.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.CompetitionRole;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "milestone_criteria_assignment")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneCriteriaAssignmentEntity extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_id", nullable = false)
    private MilestoneEntity milestone;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "criteria_id", nullable = false)
    private CriteriaEntity criteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "competition_role")
    private CompetitionRole competitionRole;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;
}
