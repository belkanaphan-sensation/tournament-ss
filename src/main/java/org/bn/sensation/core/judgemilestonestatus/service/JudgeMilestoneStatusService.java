package org.bn.sensation.core.judgemilestonestatus.service;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatusEntity;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judgemilestonestatus.service.dto.JudgeMilestoneStatusDto;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;

import jakarta.validation.constraints.NotNull;

public interface JudgeMilestoneStatusService extends BaseService<JudgeMilestoneStatusEntity, JudgeMilestoneStatusDto> {

    JudgeMilestoneStatusDto changeMilestoneStatus(@NotNull Long milestoneId, JudgeMilestoneStatus judgeRoundStatus);

    JudgeMilestoneStatusDto changeMilestoneStatus(MilestoneEntity milestone, ActivityUserEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus);

    boolean allRoundsReady(Long milestoneId);

}
