package org.bn.sensation.core.milestone.service;

import jakarta.validation.constraints.NotNull;
import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MilestoneService extends BaseCrudService<
        MilestoneEntity,
        MilestoneDto,
        CreateMilestoneRequest,
        UpdateMilestoneRequest> {

    Page<MilestoneDto> findByActivityId(@NotNull Long id, Pageable pageable);

    Page<MilestoneDto> findByActivityIdInLifeStates(@NotNull Long id, Pageable pageable);
}

