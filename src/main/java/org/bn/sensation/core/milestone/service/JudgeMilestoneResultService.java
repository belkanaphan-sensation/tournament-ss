package org.bn.sensation.core.milestone.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultRoundRequest;

public interface JudgeMilestoneResultService extends BaseCrudService<
        JudgeMilestoneResultEntity,
        JudgeMilestoneResultDto,
        JudgeMilestoneResultRoundRequest,
        JudgeMilestoneResultRoundRequest> {

    List<JudgeMilestoneResultDto> findByRoundId(Long roundId);

    List<JudgeMilestoneResultDto> findByMilestoneId(Long milestoneId);

    List<JudgeMilestoneResultDto> findByParticipantId(Long participantId);

    List<JudgeMilestoneResultDto> findByActivityUserId(Long activityUserId);

    List<JudgeMilestoneResultDto> createOrUpdateForRound(List<JudgeMilestoneResultRoundRequest> requests);

    List<JudgeMilestoneResultDto> createOrUpdateForMilestone(Long milestoneId, List<JudgeMilestoneResultMilestoneRequest> requests);
}
