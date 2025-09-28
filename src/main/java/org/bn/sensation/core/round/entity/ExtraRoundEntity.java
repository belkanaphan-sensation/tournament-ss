package org.bn.sensation.core.round.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "extra_round")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExtraRoundEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    private MilestoneEntity milestone;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    @ManyToMany(mappedBy = "extraRounds")
    @Builder.Default
    private Set<ParticipantEntity> participants = new HashSet<>();

}
