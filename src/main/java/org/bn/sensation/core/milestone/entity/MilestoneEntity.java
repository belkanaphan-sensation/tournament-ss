package org.bn.sensation.core.milestone.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.round.entity.ExtraRoundEntity;
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

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "milestone_order")
    private Integer milestoneOrder;

    @OneToMany(mappedBy = "milestone", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private Set<RoundEntity> rounds =  new HashSet<>();

    @OneToMany(mappedBy = "milestone", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private Set<ExtraRoundEntity> extraRounds =  new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;

    @OneToMany(mappedBy = "milestone", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private Set<MilestoneCriteriaAssignmentEntity> criteriaAssignments = new HashSet<>();
}
