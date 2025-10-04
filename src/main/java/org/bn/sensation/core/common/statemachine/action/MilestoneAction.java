package org.bn.sensation.core.common.statemachine.action;

import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.milestone.service.MilestoneService;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MilestoneAction implements Action<MilestoneState, MilestoneEvent> {

    private final MilestoneService milestoneService;

    @Override
    public void execute(StateContext<MilestoneState, MilestoneEvent> context) {
        MilestoneEvent event = context.getEvent();
        MilestoneState currentState = context.getSource().getId();

        // TODO: Implement business logic based on event type
        switch (event) {
            case PLAN -> {
                // Example actions for planning:
                // - Update milestone status
                // - Create initial criteria
                // - Send notifications
                // - Log planning event
            }
            case START -> {
                // Example actions for starting:
                // - Update milestone status
                // - Start related processes
                // - Send start notifications
                // - Log start event
            }
            case COMPLETE -> {
                // Example actions for completing:
                // - Update milestone status
                // - Complete related processes
                // - Generate milestone reports
                // - Send completion notifications
                // - Log completion event
            }
        }
    }
}
