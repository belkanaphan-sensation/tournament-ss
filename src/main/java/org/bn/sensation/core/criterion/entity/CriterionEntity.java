package org.bn.sensation.core.criterion.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "criterion")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CriterionEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "criterion")
    @Builder.Default
    private Set<MilestoneCriterionEntity> milestoneCriteria = new HashSet<>();
}
