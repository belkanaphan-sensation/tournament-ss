package org.bn.sensation.core.milestone.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.round.entity.RoundEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "milestone_order")
    private Integer milestoneOrder;

    @OneToMany(mappedBy = "milestone", cascade = CascadeType.REMOVE)
    @Builder.Default
    private Set<RoundEntity> rounds =  new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;

    @OneToMany(mappedBy = "milestone", cascade = CascadeType.REMOVE)
    @Builder.Default
    private Set<MilestoneCriteriaAssignmentEntity> criteriaAssignments = new HashSet<>();
}
