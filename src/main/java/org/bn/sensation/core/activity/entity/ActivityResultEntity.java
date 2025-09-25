package org.bn.sensation.core.activity.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity_result")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResultEntity extends BaseEntity {

    @Column(name = "score_sum")
    private Integer scoreSum;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;

}
