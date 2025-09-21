package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.NotNull;

public interface MilestoneCriteriaAssignmentService extends BaseCrudService<
        MilestoneCriteriaAssignmentEntity,
        MilestoneCriteriaAssignmentDto,
        CreateMilestoneCriteriaAssignmentRequest,
        UpdateMilestoneCriteriaAssignmentRequest> {

    // Custom operations
    MilestoneCriteriaAssignmentDto findByMilestoneIdAndCriteriaId(@NotNull Long milestoneId, @NotNull Long criteriaId);

    Page<MilestoneCriteriaAssignmentDto> findByMilestoneId(@NotNull Long milestoneId, Pageable pageable);

    Page<MilestoneCriteriaAssignmentDto> findByCriteriaId(@NotNull Long criteriaId, Pageable pageable);
}
