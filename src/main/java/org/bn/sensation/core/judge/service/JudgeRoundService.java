package org.bn.sensation.core.judge.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.judge.entity.JudgeRoundEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.judge.service.dto.JudgeRoundDto;

public interface JudgeRoundService extends BaseService<JudgeRoundEntity, JudgeRoundDto> {

    JudgeRoundDto changeJudgeRoundStatus(Long roundId, JudgeRoundStatus judgeRoundStatus);

    void changeJudgeRoundStatusIfPossible(Long activityUserId, Long roundId, JudgeRoundStatus judgeRoundStatus);

    JudgeRoundStatus getRoundStatusForCurrentUser(Long roundId);

    List<JudgeRoundDto> getByMilestoneIdForCurrentUser(Long milestoneId);
}
