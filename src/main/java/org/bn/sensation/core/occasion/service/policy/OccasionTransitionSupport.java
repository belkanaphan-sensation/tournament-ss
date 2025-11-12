package org.bn.sensation.core.occasion.service.policy;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.occasion.statemachine.OccasionEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.common.statemachine.policy.TransitionSupport;
import org.bn.sensation.core.occasion.statemachine.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OccasionTransitionSupport implements TransitionSupport<OccasionEntity, OccasionState, OccasionEvent> {

    @Qualifier("occasionStateServiceImpl")
    private final BaseStateService<OccasionEntity, OccasionState, OccasionEvent> occasionStateService;
    private final TransitionPolicy<OccasionEvent> occasionTransitionPolicy;

    @Override
    public Class<OccasionEntity> entityType() {
        return OccasionEntity.class;
    }

    @Override
    public BaseStateService<OccasionEntity, OccasionState, OccasionEvent> stateService() {
        return occasionStateService;
    }

    @Override
    public TransitionPolicy<OccasionEvent> transitionPolicy() {
        return occasionTransitionPolicy;
    }

    @Override
    public OccasionState currentState(OccasionEntity entity) {
        return entity.getState();
    }
}

