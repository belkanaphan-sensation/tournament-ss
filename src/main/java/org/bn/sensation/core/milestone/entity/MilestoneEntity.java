package org.bn.sensation.core.milestone.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
    private MilestoneState state;

    @Column(name = "milestone_order")
    private Integer milestoneOrder;

    @OneToOne(mappedBy = "milestone", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    private MilestoneRuleEntity milestoneRule;

    @OneToMany(mappedBy = "milestone", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private Set<RoundEntity> rounds =  new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;
}
