package org.bn.sensation.core.round.service.policy;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.round.statemachine.RoundEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.common.statemachine.policy.TransitionSupport;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoundTransitionSupport implements TransitionSupport<RoundEntity, RoundState, RoundEvent> {

    @Qualifier("roundStateServiceImpl")
    private final BaseStateService<RoundEntity, RoundState, RoundEvent> roundStateService;
    private final TransitionPolicy<RoundEvent> roundTransitionPolicy;

    @Override
    public Class<RoundEntity> entityType() {
        return RoundEntity.class;
    }

    @Override
    public BaseStateService<RoundEntity, RoundState, RoundEvent> stateService() {
        return roundStateService;
    }

    @Override
    public TransitionPolicy<RoundEvent> transitionPolicy() {
        return roundTransitionPolicy;
    }

    @Override
    public RoundState currentState(RoundEntity entity) {
        return entity.getState();
    }
}
