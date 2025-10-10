package org.bn.sensation.core.criteria.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "criteria")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "criteria")
    @Builder.Default
    private Set<MilestoneCriteriaAssignmentEntity> milestoneAssignments = new HashSet<>();
}
