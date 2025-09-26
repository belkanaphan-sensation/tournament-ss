package org.bn.sensation.core.round.service;

import java.util.List;

import jakarta.validation.constraints.NotNull;
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

    List<RoundDto> findByMilestoneId(@NotNull Long id);

    List<RoundDto> findByMilestoneIdInLifeStates(@NotNull Long id);
}
