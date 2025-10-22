package org.bn.sensation.core.milestoneresult.repository;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;

import jakarta.persistence.EntityNotFoundException;

public interface MilestoneResultRepository extends BaseRepository<MilestoneResultEntity> {

    default MilestoneResultEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Результат этапа не найден: " + id));
    }
}
