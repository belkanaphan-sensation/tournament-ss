package org.bn.sensation.core.criteria.entity;

import java.math.BigDecimal;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
    @Column(name = "partner_side")
    private PartnerSide partnerSide;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "scale", nullable = false)
    private Integer scale;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;
}
