package org.bn.sensation.core.milestonecriterion.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.service.dto.CreateMilestoneCriterionRequest;
import org.bn.sensation.core.milestonecriterion.service.dto.MilestoneCriterionDto;
import org.bn.sensation.core.milestonecriterion.service.dto.UpdateMilestoneCriterionRequest;

import jakarta.validation.constraints.NotNull;

public interface MilestoneCriterionService extends BaseCrudService<
        MilestoneCriterionEntity,
        MilestoneCriterionDto,
        CreateMilestoneCriterionRequest,
        UpdateMilestoneCriterionRequest> {

    MilestoneCriterionDto findByMilestoneRuleIdAndCriterionId(@NotNull Long milestoneRuleId, @NotNull Long criteriaId);

    MilestoneCriterionDto findByMilestoneIdAndCriterionId(@NotNull Long milestoneId, @NotNull Long criteriaId);

    List<MilestoneCriterionDto> findByMilestoneId(@NotNull Long milestoneId);

    List<MilestoneCriterionDto> findByMilestoneRuleId(@NotNull Long milestoneRuleId);

    List<MilestoneCriterionDto> findByMilestoneRuleIdForCurrentUser(@NotNull Long milestoneRuleId);

    List<MilestoneCriterionDto> findByMilestoneIdForCurrentUser(@NotNull Long milestoneId);

    List<MilestoneCriterionDto> findByCriterionId(@NotNull Long criteriaId);
}
