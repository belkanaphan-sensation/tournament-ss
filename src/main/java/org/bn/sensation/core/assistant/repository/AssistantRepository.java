package org.bn.sensation.core.assistant.repository;

import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.common.repository.BaseRepository;

import jakarta.persistence.EntityNotFoundException;

public interface AssistantRepository extends BaseRepository<AssistantEntity> {

    default AssistantEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Ассистент не найден: " + id));
    }
}
