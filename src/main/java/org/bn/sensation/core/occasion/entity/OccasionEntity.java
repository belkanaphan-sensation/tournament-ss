package org.bn.sensation.core.occasion.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.State;
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
    private LocalDate startDate; // 13.04.2025

    @Column(name = "end_date")
    private LocalDate endDate; // 15.04.2025

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "occasion", cascade = CascadeType.REMOVE)
    @Builder.Default
    private Set<ActivityEntity> activities = new HashSet<>();
}
