package org.bn.sensation.core.milestoneresult.repository;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestoneresult.entity.MilestoneRoundResultEntity;

import jakarta.persistence.EntityNotFoundException;

public interface MilestoneRoundResultRepository extends BaseRepository<MilestoneRoundResultEntity> {

    default MilestoneRoundResultEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Результат раунда для этапа не найден: " + id));
    }
}
