package org.bn.sensation.core.judge.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.judge.entity.JudgeMilestoneEntity;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judge.service.dto.JudgeMilestoneDto;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;

import jakarta.validation.constraints.NotNull;

public interface JudgeMilestoneService extends BaseService<JudgeMilestoneEntity, JudgeMilestoneDto> {

    JudgeMilestoneDto changeMilestoneStatus(@NotNull Long milestoneId, JudgeMilestoneStatus judgeRoundStatus);

    JudgeMilestoneDto changeMilestoneStatus(MilestoneEntity milestone, UserActivityAssignmentEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus);

    boolean allRoundsReady(Long milestoneId);

}
