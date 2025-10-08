package org.bn.sensation.core.milestone.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.CreateJudgeMilestoneResultRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.UpdateJudgeMilestoneResultRequest;

public interface JudgeMilestoneResultService extends BaseCrudService<
        JudgeMilestoneResultEntity,
        JudgeMilestoneResultDto,
        CreateJudgeMilestoneResultRequest,
        UpdateJudgeMilestoneResultRequest> {

    List<JudgeMilestoneResultDto> findByRoundId(Long roundId);

    List<JudgeMilestoneResultDto> findByMilestoneId(Long milestoneId);

    List<JudgeMilestoneResultDto> findByParticipantId(Long participantId);

    List<JudgeMilestoneResultDto> findByActivityUserId(Long activityUserId);
}
