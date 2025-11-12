package org.bn.sensation.core.round.statemachine;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoundAction implements Action<RoundState, RoundEvent> {

    private final BaseStateService<RoundEntity, RoundState, RoundEvent> roundStateService;

    @Override
    public void execute(StateContext<RoundState, RoundEvent> context) {
        RoundEntity round = (RoundEntity) context.getMessageHeader("round");
        RoundState target = context.getTarget().getId();
        roundStateService.saveTransition(round, target);
    }
}
