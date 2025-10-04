package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;

public interface OccasionService extends BaseCrudService<
        OccasionEntity,
        OccasionDto,
        CreateOccasionRequest,
        UpdateOccasionRequest>, BaseStateService<OccasionEntity, OccasionState, OccasionEvent> {
}
