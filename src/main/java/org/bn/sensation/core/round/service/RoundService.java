package org.bn.sensation.core.round.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public interface RoundService extends BaseCrudService<
        RoundEntity,
        RoundDto,
        CreateRoundRequest,
        UpdateRoundRequest>{

    List<RoundDto> findByMilestoneId(@NotNull Long id);

    List<RoundWithJRStatusDto> findByMilestoneIdInLifeStates(@NotNull Long id);

    List<RoundDto> generateRounds(MilestoneEntity milestone, @Nullable List<Long> participantIds, @Nullable Boolean reGenerate);

    void draftRound(Long id);

    void planRound(Long id);

    void startRound(Long id);

    void completeRound(Long id);
}
