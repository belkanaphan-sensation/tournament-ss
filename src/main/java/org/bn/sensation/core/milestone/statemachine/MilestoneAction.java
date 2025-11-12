package org.bn.sensation.core.milestone.statemachine;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MilestoneAction implements Action<MilestoneState, MilestoneEvent> {

    private final BaseStateService<MilestoneEntity, MilestoneState, MilestoneEvent> milestoneStateService;

    @Override
    public void execute(StateContext<MilestoneState, MilestoneEvent> context) {
        MilestoneState target = context.getTarget().getId();
        MilestoneEntity milestone = (MilestoneEntity) context.getMessageHeader("milestone");
        milestoneStateService.saveTransition(milestone, target);
    }
}
