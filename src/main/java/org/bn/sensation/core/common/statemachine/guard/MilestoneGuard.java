package org.bn.sensation.core.common.statemachine.guard;

import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MilestoneGuard implements Guard<MilestoneState, MilestoneEvent> {

    @Override
    public boolean evaluate(StateContext<MilestoneState, MilestoneEvent> context) {
        return true;
    }
}
