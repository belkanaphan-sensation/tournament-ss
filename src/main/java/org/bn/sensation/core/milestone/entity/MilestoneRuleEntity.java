package org.bn.sensation.core.milestone.entity;

import org.bn.sensation.core.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    //не уверена что это должна быть энтити
}
