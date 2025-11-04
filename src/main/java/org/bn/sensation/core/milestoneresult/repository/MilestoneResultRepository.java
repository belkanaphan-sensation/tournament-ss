package org.bn.sensation.core.milestoneresult.repository;

import java.util.List;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.springframework.data.jpa.repository.EntityGraph;

import jakarta.persistence.EntityNotFoundException;

public interface MilestoneResultRepository extends BaseRepository<MilestoneResultEntity> {

    @EntityGraph(attributePaths = {"milestone", "participant", "roundResults.round"})
    List<MilestoneResultEntity> findAllByMilestoneId(Long milestoneId);

    default MilestoneResultEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Результат этапа не найден: " + id));
    }
}
