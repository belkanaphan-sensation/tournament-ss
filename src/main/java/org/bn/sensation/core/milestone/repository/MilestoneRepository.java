package org.bn.sensation.core.milestone.repository;

import java.util.List;

import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface MilestoneRepository extends BaseRepository<MilestoneEntity> {

    /**
     * Найти этапы активности, отсортированные по порядку
     */
    Page<MilestoneEntity> findByActivityIdOrderByMilestoneOrderAsc(Long activityId, Pageable pageable);

    /**
     * Найти все этапы активности, отсортированные по порядку
     */
    List<MilestoneEntity> findByActivityIdOrderByMilestoneOrderAsc(Long activityId);

    /**
     * Найти этапы активности в life states, отсортированные по порядку
     */
    Page<MilestoneEntity> findByActivityIdAndStateInOrderByMilestoneOrderAsc(@Param("activityId") Long activityId, Pageable pageable, @Param("states") List<State> states);

}
