package org.bn.sensation.core.round.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public interface RoundService extends BaseService<RoundEntity, RoundDto> {

    RoundDto createExtraRound(CreateRoundRequest request);

    void deleteById(Long id);

    List<RoundDto> findByMilestoneId(@NotNull Long id);

    List<RoundWithJRStatusDto> findByMilestoneIdInLifeStates(@NotNull Long id);

    List<RoundDto> generateRounds(MilestoneEntity milestone, boolean reGenerate, @Nullable Integer roundParticipantLimit);

}
