package org.bn.sensation.core.milestoneresult.service;

import java.util.List;
import java.util.Map;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;

import jakarta.validation.constraints.NotNull;

public interface MilestoneResultService extends BaseService<MilestoneResultEntity, MilestoneResultDto> {

    List<MilestoneResultDto> calculateResults(MilestoneEntity milestone);

    List<MilestoneResultDto> acceptResults(Long milestoneId, List<UpdateMilestoneResultRequest> request);

    List<MilestoneResultDto> getByMilestoneId(@NotNull Long milestoneId);

    Map<Integer, List<MilestoneResultDto>> getByActivityId(@NotNull Long activityId);
}
