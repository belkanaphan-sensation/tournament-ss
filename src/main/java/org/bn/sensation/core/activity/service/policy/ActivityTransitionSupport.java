package org.bn.sensation.core.activity.service.policy;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.activity.statemachine.ActivityEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.common.statemachine.policy.TransitionSupport;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityTransitionSupport implements TransitionSupport<ActivityEntity, ActivityState, ActivityEvent> {

    @Qualifier("activityStateServiceImpl")
    private final BaseStateService<ActivityEntity, ActivityState, ActivityEvent> activityStateService;
    private final TransitionPolicy<ActivityEvent> activityTransitionPolicy;

    @Override
    public Class<ActivityEntity> entityType() {
        return ActivityEntity.class;
    }

    @Override
    public BaseStateService<ActivityEntity, ActivityState, ActivityEvent> stateService() {
        return activityStateService;
    }

    @Override
    public TransitionPolicy<ActivityEvent> transitionPolicy() {
        return activityTransitionPolicy;
    }

    @Override
    public ActivityState currentState(ActivityEntity entity) {
        return entity.getState();
    }
}
