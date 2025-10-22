package org.bn.sensation.core.criterion.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.service.dto.CriterionRequest;
import org.bn.sensation.core.criterion.service.dto.CriterionDto;

public interface CriterionService extends BaseCrudService<
        CriterionEntity,
        CriterionDto,
        CriterionRequest,
        CriterionRequest> {
}
