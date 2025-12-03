package org.bn.sensation.core.assistant.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.participant.entity.ParticipantEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assistant")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssistantEntity extends BaseEntity {

    @Column(name = "nick_name")
    private String nickName;

    @Embedded
    private Person person;

    @Column(name = "school")
    private String school;

    @OneToMany(mappedBy = "assistant", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private Set<ParticipantEntity> participants = new HashSet<>();

}
