package org.bn.sensation.core.occasion.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.organization.entity.OrganizationEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "occasion")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OccasionEntity extends BaseEntity {

    @Column(name = "name")
    private String name; // SBF

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private ZonedDateTime startDate; // 2025-09-18T16:56:47+04:00[Europe/Samara]

    @Column(name = "end_date")
    private ZonedDateTime endDate; // 2025-09-18T16:56:47+04:00[Europe/Samara]

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "occasion", cascade = CascadeType.REMOVE)
    @Builder.Default
    private Set<ActivityEntity> activities = new HashSet<>();
}
