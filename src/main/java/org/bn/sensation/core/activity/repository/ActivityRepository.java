package org.bn.sensation.core.activity.repository;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityRepository extends BaseRepository<ActivityEntity> {

    Page<ActivityEntity> findByOccasionId(Long occasionId, Pageable pageable);

    /**
     * Найти активности для мероприятия по нескольким статусам
     */
    Page<ActivityEntity> findByOccasionIdAndStateIn(Long occasionId, Pageable pageable, List<State> states);

}
