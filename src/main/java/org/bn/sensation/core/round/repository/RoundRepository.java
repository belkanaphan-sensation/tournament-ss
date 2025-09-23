package org.bn.sensation.core.round.repository;

import java.util.List;

import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface RoundRepository extends BaseRepository<RoundEntity> {

    Page<RoundEntity> findByMilestoneId(Long milestoneId, Pageable pageable);

    /**
     * Подсчитать количество раундов для этапа по статусу
     */
    long countByMilestoneIdAndState(Long milestoneId, State state);

    /**
     * Подсчитать общее количество раундов для этапа
     */
    long countByMilestoneId(Long milestoneId);

    /**
     * Найти раунды этапа в life states
     */
    Page<RoundEntity> findByMilestoneIdAndStateIn(@Param("milestoneId") Long milestoneId, Pageable pageable, @Param("states") List<State> states);
}
