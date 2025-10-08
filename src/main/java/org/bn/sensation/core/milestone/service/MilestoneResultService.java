package org.bn.sensation.core.milestone.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;

public interface MilestoneResultService extends BaseService<MilestoneResultEntity, MilestoneResultDto> {

    List<MilestoneResultDto> getByMilestoneId(Long milestoneId);

    void update(MilestoneResultDto milestoneResultDto);
}
