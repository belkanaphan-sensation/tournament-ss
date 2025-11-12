package org.bn.sensation.core.occasion.statemachine;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OccasionAction implements Action<OccasionState, OccasionEvent> {

    private final BaseStateService<OccasionEntity, OccasionState, OccasionEvent> occasionStateService;

    @Override
    public void execute(StateContext<OccasionState, OccasionEvent> context) {
        OccasionEntity occasion = (OccasionEntity) context.getMessageHeader("occasion");
        OccasionState target = context.getTarget().getId();
        occasionStateService.saveTransition(occasion, target);
    }
}
