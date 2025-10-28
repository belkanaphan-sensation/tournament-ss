package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;

public interface OccasionService extends BaseCrudService<
        OccasionEntity,
        OccasionDto,
        CreateOccasionRequest,
        UpdateOccasionRequest> {

    void planOccasion(Long id);

    void startOccasion(Long id);

    void completeOccasion(Long id);
}
