package org.bn.sensation.core.milestone.repository;

import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;

public interface MilestoneRepository extends BaseRepository<MilestoneEntity> {

    /**
     * Подсчитать количество этапов для активности по статусу
     */
    long countByActivityIdAndStatus(Long activityId, Status status);

    /**
     * Подсчитать общее количество этапов для активности
     */
    long countByActivityId(Long activityId);
}
