package org.bn.sensation.core.contestant.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contestant")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContestantEntity extends BaseEntity {

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "contestant_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestantType contestantType;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "contestants_participants_association",
            joinColumns = @JoinColumn(name = "contestant_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id"))
    @Builder.Default
    private Set<ParticipantEntity> participants = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "contestants_milestones_association",
            joinColumns = @JoinColumn(name = "contestant_id"),
            inverseJoinColumns = @JoinColumn(name = "milestone_id"))
    @Builder.Default
    private Set<MilestoneEntity> milestones = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "contestants_rounds_association",
            joinColumns = @JoinColumn(name = "contestant_id"),
            inverseJoinColumns = @JoinColumn(name = "round_id"))
    @Builder.Default
    private Set<RoundEntity> rounds = new HashSet<>();
}
