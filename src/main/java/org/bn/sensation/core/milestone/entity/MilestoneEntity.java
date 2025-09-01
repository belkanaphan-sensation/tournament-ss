package org.bn.sensation.core.milestone.entity;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.round.entity.RoundEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @OneToMany(mappedBy = "milestone")
    private List<RoundEntity> rounds;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "activity_id")
    private ActivityEntity activity;
}
