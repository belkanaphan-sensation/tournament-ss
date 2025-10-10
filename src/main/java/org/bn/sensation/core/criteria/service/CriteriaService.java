package org.bn.sensation.core.criteria.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.service.dto.CriteriaRequest;
import org.bn.sensation.core.criteria.service.dto.CriteriaDto;

public interface CriteriaService extends BaseCrudService<
        CriteriaEntity,
        CriteriaDto,
        CriteriaRequest,
        CriteriaRequest> {
}
