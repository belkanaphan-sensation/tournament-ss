package org.bn.sensation.core.participant.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.round.entity.RoundEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "participant")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEntity extends BaseEntity {

    @Embedded
    private Person person;

    // Номер который присваивается например на джеке или еще где-то
    @Column(name = "number")
    private String number;

    @Column(name = "partner_side")
    @Enumerated(EnumType.STRING)
    private PartnerSide partnerSide;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "rounds_participants_association",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "round_id"))
    @Builder.Default
    private Set<RoundEntity> rounds = new HashSet<>();
}
