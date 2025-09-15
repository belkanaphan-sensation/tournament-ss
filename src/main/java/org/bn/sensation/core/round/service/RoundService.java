package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;

public interface RoundService extends BaseCrudService<
        RoundEntity,
        RoundDto,
        CreateRoundRequest,
        UpdateRoundRequest> {
}
