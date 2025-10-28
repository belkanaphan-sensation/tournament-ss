package org.bn.sensation.core.judgemilestoneresult.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultRoundRequest;

public interface JudgeMilestoneResultService extends BaseCrudService<
        JudgeMilestoneResultEntity,
        JudgeMilestoneResultDto,
        JudgeMilestoneResultRoundRequest,
        JudgeMilestoneResultRoundRequest> {

    JudgeMilestoneResultDto createOrUpdate(JudgeMilestoneResultRoundRequest request, Long activityUserId);

    List<JudgeMilestoneResultDto> findByRoundId(Long roundId);

    List<JudgeMilestoneResultDto> findByMilestoneId(Long milestoneId);

    List<JudgeMilestoneResultDto> findByRoundIdForCurrentUser(Long roundId);

    List<JudgeMilestoneResultDto> findByMilestoneIdForCurrentUser(Long milestoneId);

    List<JudgeMilestoneResultDto> findByParticipantId(Long participantId);

    List<JudgeMilestoneResultDto> findByActivityUserId(Long activityUserId);

    List<JudgeMilestoneResultDto> createOrUpdateForRound(Long roundId, List<JudgeMilestoneResultRoundRequest> requests);
}
