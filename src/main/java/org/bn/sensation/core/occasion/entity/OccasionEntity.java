package org.bn.sensation.core.occasion.entity;

import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;
import org.bn.sensation.common.entity.BaseEntity;
import org.bn.sensation.core.activity.entity.ActivityEntity;

public class OccasionEntity extends BaseEntity {

    private String name; // SBF

    private String description;

    private LocalDate startDate; // 13.04.2025

    private LocalDate endDate; // 15.04.2025

    @OneToMany private List<ActivityEntity> activities;
}
