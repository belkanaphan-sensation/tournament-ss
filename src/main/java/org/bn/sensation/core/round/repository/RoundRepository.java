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

    List<RoundEntity> findByMilestoneId(Long milestoneId);

    List<RoundEntity> findByMilestoneIdAndStateIn(@Param("milestoneId") Long milestoneId, @Param("states") List<State> states);
}
