package org.bn.sensation.core.judgeroundstatus.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.service.dto.JudgeRoundStatusDto;

public interface JudgeRoundStatusService extends BaseService<JudgeRoundStatusEntity, JudgeRoundStatusDto> {

    JudgeRoundStatusDto changeJudgeRoundStatus(Long roundId, JudgeRoundStatus judgeRoundStatus);

    void changeJudgeRoundStatusIfPossible(Long activityUserId, Long roundId, JudgeRoundStatus judgeRoundStatus);

    JudgeRoundStatus getRoundStatusForCurrentUser(Long roundId);

    List<JudgeRoundStatusDto> getByMilestoneIdForCurrentUser(Long milestoneId);
}
