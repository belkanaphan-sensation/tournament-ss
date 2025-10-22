package org.bn.sensation.core.activityresult.repository;

import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.common.repository.BaseRepository;

import jakarta.persistence.EntityNotFoundException;

public interface ActivityResultRepository extends BaseRepository<ActivityResultEntity> {

    default ActivityResultEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Результат активности не найден: " + id));
    }
}
