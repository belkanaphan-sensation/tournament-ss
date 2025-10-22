package org.bn.sensation.core.occasion.repository;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;

import jakarta.persistence.EntityNotFoundException;

public interface OccasionRepository extends BaseRepository<OccasionEntity> {

    default OccasionEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено: " + id));
    }
}
