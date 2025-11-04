package org.bn.sensation.core.milestoneresult.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.CreateMilestoneResultRequest;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;

public interface MilestoneResultService extends BaseCrudService<
        MilestoneResultEntity,
        MilestoneResultDto,
        CreateMilestoneResultRequest,
        UpdateMilestoneResultRequest> {

    List<MilestoneResultDto> calculateResults(MilestoneEntity milestone);

    List<MilestoneResultDto> acceptResults(MilestoneEntity milestone, List<UpdateMilestoneResultRequest> request);

}
