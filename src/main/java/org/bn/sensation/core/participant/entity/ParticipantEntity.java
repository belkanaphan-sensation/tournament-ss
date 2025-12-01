package org.bn.sensation.core.participant.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.contestant.entity.ContestantEntity;

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

    @Column(name = "school")
    private String school;

    // Номер который присваивается например на джеке или еще где-то
    @Column(name = "number")
    private String number;

    @Column(name = "partner_side")
    @Enumerated(EnumType.STRING)
    private PartnerSide partnerSide;

    @Column(name = "is_registered")
    private Boolean isRegistered;

    @ManyToOne
    @JoinColumn(name = "assistant_id")
    private AssistantEntity assistant;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;

    @ManyToMany(mappedBy = "participants")
    @Builder.Default
    private Set<ContestantEntity> contestants = new HashSet<>();

}
