package org.bn.sensation.core.milestoneresult.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.*;

import jakarta.validation.constraints.NotNull;

public interface MilestoneResultService extends BaseCrudService<
        MilestoneResultEntity,
        MilestoneResultDto,
        CreateMilestoneResultRequest,
        UpdateMilestoneResultRequest> {

    List<MilestoneResultDto> calculateResults(MilestoneEntity milestone);

    List<MilestoneResultDto> acceptResults(Long milestoneId, List<UpdateMilestoneResultRequest> request);

    List<MilestoneResultEntity> acceptResults(MilestoneEntity milestone, List<UpdateMilestoneResultRequest> request);

    MilestoneRoundResultDto createMilestoneRoundResult(CreateMilestoneRoundResultRequest request);

    List<MilestoneResultDto> getByMilestoneId(@NotNull Long milestoneId);
}
