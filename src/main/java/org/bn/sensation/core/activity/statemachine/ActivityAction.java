package org.bn.sensation.core.activity.statemachine;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.service.BaseStateService;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityAction implements Action<ActivityState, ActivityEvent> {

    private final BaseStateService<ActivityEntity, ActivityState, ActivityEvent> activityStateService;

    @Override
    public void execute(StateContext<ActivityState, ActivityEvent> context) {
        ActivityEntity activity = (ActivityEntity) context.getMessageHeader("activity");
        ActivityState target = context.getTarget().getId();
        activityStateService.saveTransition(activity, target);
    }
}
