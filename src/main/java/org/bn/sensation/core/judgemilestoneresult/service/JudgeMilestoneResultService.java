package org.bn.sensation.core.judgemilestoneresult.service;

import java.util.Collection;
import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultRoundRequest;

public interface JudgeMilestoneResultService extends BaseService<
        JudgeMilestoneResultEntity,
        JudgeMilestoneResultDto> {

    List<JudgeMilestoneResultDto> findByRoundId(Long roundId);

    List<JudgeMilestoneResultDto> findByMilestoneId(Long milestoneId);

    List<JudgeMilestoneResultDto> findByRoundIdForCurrentUser(Long roundId);

    List<JudgeMilestoneResultDto> findByMilestoneIdForCurrentUser(Long milestoneId);

    List<JudgeMilestoneResultDto> findByContestantId(Long participantId);

    List<JudgeMilestoneResultDto> findByContestantIdAndMilestoneId(Collection<Long> participantIds, Long milestoneId);

    List<JudgeMilestoneResultDto> findByActivityUserId(Long activityUserId);

    List<JudgeMilestoneResultDto> createOrUpdateForRound(Long roundId, List<JudgeMilestoneResultRoundRequest> requests);
}
