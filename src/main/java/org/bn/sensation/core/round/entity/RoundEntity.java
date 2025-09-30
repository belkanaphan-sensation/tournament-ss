package org.bn.sensation.core.round.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "round")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoundEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    private MilestoneEntity milestone;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoundState state;

    @ManyToMany(mappedBy = "rounds")
    @Builder.Default
    private Set<ParticipantEntity> participants = new HashSet<>();
}
