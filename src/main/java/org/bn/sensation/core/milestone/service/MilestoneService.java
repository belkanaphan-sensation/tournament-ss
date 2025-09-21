package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;

public interface MilestoneService extends BaseCrudService<
        MilestoneEntity,
        MilestoneDto,
        CreateMilestoneRequest,
        UpdateMilestoneRequest> {
}

