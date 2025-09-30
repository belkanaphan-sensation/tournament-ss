package org.bn.sensation.core.common.statemachine.action;

import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.RoundService;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoundAction implements Action<RoundState, RoundEvent> {

    private final RoundService roundService;

    @Override
    public void execute(StateContext<RoundState, RoundEvent> context) {
        RoundEntity round = (RoundEntity) context.getMessageHeader("round");
        RoundState target = context.getTarget().getId();
        roundService.saveTransition(round, target);
    }
}
