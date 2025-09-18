package org.bn.sensation.core.activity.repository;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.Status;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityRepository extends BaseRepository<ActivityEntity> {

    Page<ActivityEntity> findByOccasionId(Long occasionId, Pageable pageable);

    /**
     * Подсчитать количество активностей для мероприятия по статусу
     */
    long countByOccasionIdAndStatus(Long occasionId, Status status);

    /**
     * Подсчитать количество активностей для мероприятия по нескольким статусам
     */
    long countByOccasionIdAndStatusIn(Long occasionId, Status... statuses);

    /**
     * Подсчитать общее количество активностей для мероприятия
     */
    long countByOccasionId(Long occasionId);
}
