package org.bn.sensation.core.milestonecriterion.entity;

import java.math.BigDecimal;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone_criterion")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneCriterionEntity extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_rule_id", nullable = false)
    private MilestoneRuleEntity milestoneRule;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "criterion_id", nullable = false)
    private CriterionEntity criterion;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner_side")
    private PartnerSide partnerSide;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "scale", nullable = false)
    private Integer scale;

}
