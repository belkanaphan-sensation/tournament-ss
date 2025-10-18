package org.bn.sensation.core.milestonecriteria.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestonecriteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestonecriteria.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.milestonecriteria.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.milestonecriteria.service.dto.UpdateMilestoneCriteriaAssignmentRequest;

import jakarta.validation.constraints.NotNull;

public interface MilestoneCriteriaAssignmentService extends BaseCrudService<
        MilestoneCriteriaAssignmentEntity,
        MilestoneCriteriaAssignmentDto,
        CreateMilestoneCriteriaAssignmentRequest,
        UpdateMilestoneCriteriaAssignmentRequest> {

    MilestoneCriteriaAssignmentDto findByMilestoneRuleIdAndCriteriaId(@NotNull Long milestoneRuleId, @NotNull Long criteriaId);

    MilestoneCriteriaAssignmentDto findByMilestoneIdAndCriteriaId(@NotNull Long milestoneId, @NotNull Long criteriaId);

    List<MilestoneCriteriaAssignmentDto> findByMilestoneId(@NotNull Long milestoneId);

    List<MilestoneCriteriaAssignmentDto> findByMilestoneRuleId(@NotNull Long milestoneRuleId);

    List<MilestoneCriteriaAssignmentDto> findByMilestoneRuleIdForCurrentUser(@NotNull Long milestoneRuleId);

    List<MilestoneCriteriaAssignmentDto> findByMilestoneIdForCurrentUser(@NotNull Long milestoneId);

    List<MilestoneCriteriaAssignmentDto> findByCriteriaId(@NotNull Long criteriaId);
}
