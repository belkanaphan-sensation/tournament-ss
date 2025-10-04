package org.bn.sensation.core.common.statemachine.action;

import org.bn.sensation.core.activity.service.ActivityService;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityAction implements Action<ActivityState, ActivityEvent> {

    private final ActivityService activityService;

    @Override
    public void execute(StateContext<ActivityState, ActivityEvent> context) {
        ActivityEvent event = context.getEvent();
        ActivityState currentState = context.getSource().getId();
//        Long stageId = context.getExtendedState().get("stageId", Long.class);

        // TODO: Implement business logic based on event type
        switch (event) {
            case PLAN -> {
                // Example actions for planning:
                // - Update activity status
                // - Create initial milestones
                // - Send notifications
                // - Log planning event
            }
            case START -> {
                // Example actions for starting:
                // - Update activity status
                // - Start related milestones
                // - Send start notifications
                // - Log start event
            }
            case COMPLETE -> {
                // Example actions for completing:
                // - Update activity status
                // - Complete related milestones
                // - Generate activity reports
                // - Send completion notifications
                // - Log completion event
            }
        }
    }
}
