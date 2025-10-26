package org.bn.sensation.core.round.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface RoundService extends BaseCrudService<
        RoundEntity,
        RoundDto,
        CreateRoundRequest,
        UpdateRoundRequest>, BaseStateService<RoundEntity, RoundState, RoundEvent> {

    List<RoundDto> findByMilestoneId(@NotNull Long id);

    List<RoundWithJRStatusDto> findByMilestoneIdInLifeStates(@NotNull Long id);

    List<RoundDto> generateRounds(@Valid GenerateRoundsRequest request);
}
