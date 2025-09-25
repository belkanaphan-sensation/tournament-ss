package org.bn.sensation.core.milestone.entity;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "milestone_result")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResultEntity extends BaseEntity {

    @Column(name = "score_sum")
    private Integer scoreSum;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "milestone_id")
    private MilestoneEntity milestone;

}
