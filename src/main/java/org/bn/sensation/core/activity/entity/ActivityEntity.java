package org.bn.sensation.core.activity.entity;

import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.experimental.SuperBuilder;
import org.bn.sensation.common.entity.BaseEntity;
import org.bn.sensation.core.occasion.entity.OccasionEntity;

@SuperBuilder(toBuilder = true)
public class ActivityEntity extends BaseEntity {

    private String name; // Jack and Jill

    private String description;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @ManyToOne private OccasionEntity occasion;
}
