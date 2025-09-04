package org.bn.sensation.core.activity.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.occasion.entity.OccasionEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntity extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Embedded
    private Address address;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "occasion_id")
    private OccasionEntity occasion;

    @OneToMany(mappedBy = "activity")
    private List<MilestoneEntity> milestones;
}
