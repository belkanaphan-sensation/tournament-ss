package org.bn.sensation.core.common.statemachine.action;

import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.occasion.service.OccasionService;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OccasionAction implements Action<OccasionState, OccasionEvent> {

    private final OccasionService occasionService;

    @Override
    public void execute(StateContext<OccasionState, OccasionEvent> context) {
        OccasionEvent event = context.getEvent();
        OccasionState currentState = context.getSource().getId();

        // TODO: Implement business logic based on event type
        switch (event) {
            case PLAN -> {
                // Example actions for planning:
                // - Update occasion status
                // - Create initial activities
                // - Send notifications
                // - Log planning event
            }
            case START -> {
                // Example actions for starting:
                // - Update occasion status
                // - Start related activities
                // - Send start notifications
                // - Log start event
            }
            case COMPLETE -> {
                // Example actions for completing:
                // - Update occasion status
                // - Complete related activities
                // - Generate reports
                // - Send completion notifications
                // - Log completion event
            }
        }
    }
}
