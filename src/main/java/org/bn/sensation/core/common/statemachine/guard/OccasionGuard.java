package org.bn.sensation.core.common.statemachine.guard;

import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OccasionGuard implements Guard<OccasionState, OccasionEvent> {

    @Override
    public boolean evaluate(StateContext<OccasionState, OccasionEvent> context) {
        return true;
    }
}
