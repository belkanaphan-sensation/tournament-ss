package org.bn.sensation.core.milestone.repository;

import java.util.List;

import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MilestoneRepository extends BaseRepository<MilestoneEntity> {

    /**
     * Подсчитать количество этапов для активности по статусу
     */
    long countByActivityIdAndStatus(Long activityId, State state);

    /**
     * Подсчитать общее количество этапов для активности
     */
    long countByActivityId(Long activityId);

    /**
     * Найти этапы активности, отсортированные по порядку
     */
    Page<MilestoneEntity> findByActivityIdOrderByMilestoneOrderAsc(Long activityId, Pageable pageable);

    /**
     * Найти все этапы активности, отсортированные по порядку
     */
    List<MilestoneEntity> findByActivityIdOrderByMilestoneOrderAsc(Long activityId);

}
