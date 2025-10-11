package org.bn.sensation.core.milestone.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneDto;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;

import jakarta.validation.constraints.NotNull;

public interface MilestoneService extends BaseCrudService<
        MilestoneEntity,
        MilestoneDto,
        CreateMilestoneRequest,
        UpdateMilestoneRequest>, BaseStateService<MilestoneEntity, MilestoneState, MilestoneEvent> {

    List<MilestoneDto> findByActivityId(@NotNull Long id);

    List<MilestoneDto> findByActivityIdInLifeStates(@NotNull Long id);

    JudgeMilestoneDto changeMilestoneStatus(@NotNull Long milestoneId, JudgeMilestoneStatus judgeRoundStatus);

    JudgeMilestoneDto changeMilestoneStatus(MilestoneEntity milestone, UserActivityAssignmentEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus);

    boolean allRoundsReady(Long milestoneId);
}
