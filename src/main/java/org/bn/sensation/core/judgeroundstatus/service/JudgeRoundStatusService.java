package org.bn.sensation.core.judgeroundstatus.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.service.dto.JudgeRoundStatusDto;

import jakarta.validation.constraints.NotNull;

public interface JudgeRoundStatusService extends BaseService<JudgeRoundStatusEntity, JudgeRoundStatusDto> {

    JudgeRoundStatusDto markNotReady(Long roundId);

    JudgeRoundStatus getRoundStatusForCurrentUser(Long roundId);

    List<JudgeRoundStatusDto> getByMilestoneIdForCurrentUser(Long milestoneId);

    List<JudgeRoundStatusDto> getByRoundId(@NotNull Long roundId);

    void invalidateForRound(Long roundId);
}
