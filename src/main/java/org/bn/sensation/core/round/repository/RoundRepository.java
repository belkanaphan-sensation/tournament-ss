package org.bn.sensation.core.round.repository;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoundRepository extends BaseRepository<RoundEntity> {

    Page<RoundEntity> findByMilestoneId(Long milestoneId, Pageable pageable);
}
