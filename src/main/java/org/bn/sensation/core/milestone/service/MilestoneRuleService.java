package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRuleRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneRuleDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRuleRequest;

import jakarta.validation.constraints.NotNull;

public interface MilestoneRuleService extends BaseCrudService<
        MilestoneRuleEntity,
        MilestoneRuleDto,
        CreateMilestoneRuleRequest,
        UpdateMilestoneRuleRequest> {

    MilestoneRuleDto findByMilestoneId(@NotNull Long milestoneId);
}
