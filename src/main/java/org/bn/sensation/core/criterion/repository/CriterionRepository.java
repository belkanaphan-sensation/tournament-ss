package org.bn.sensation.core.criterion.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityNotFoundException;

@Repository
public interface CriterionRepository extends BaseRepository<CriterionEntity> {

    Optional<CriterionEntity> findByName(String name);

    default CriterionEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Критерий не найден: " + id));
    }
}
