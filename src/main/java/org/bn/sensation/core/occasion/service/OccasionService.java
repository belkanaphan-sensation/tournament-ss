package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OccasionService extends BaseService<OccasionEntity, OccasionDto> {

    // CRUD operations
    Page<OccasionDto> findAll(Pageable pageable);

    OccasionDto create(CreateOccasionRequest request);

    OccasionDto update(Long id, UpdateOccasionRequest request);

    void deleteById(Long id);
}
