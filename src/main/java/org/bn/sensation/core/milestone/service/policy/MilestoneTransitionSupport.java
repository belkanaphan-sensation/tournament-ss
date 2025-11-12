package org.bn.sensation.core.milestone.service.policy;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.common.statemachine.policy.TransitionSupport;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MilestoneTransitionSupport implements TransitionSupport<MilestoneEntity, MilestoneState, MilestoneEvent> {

    @Qualifier("milestoneStateServiceImpl")
    private final BaseStateService<MilestoneEntity, MilestoneState, MilestoneEvent> milestoneStateService;
    private final TransitionPolicy<MilestoneEvent> milestoneTransitionPolicy;

    @Override
    public Class<MilestoneEntity> entityType() {
        return MilestoneEntity.class;
    }

    @Override
    public BaseStateService<MilestoneEntity, MilestoneState, MilestoneEvent> stateService() {
        return milestoneStateService;
    }

    @Override
    public TransitionPolicy<MilestoneEvent> transitionPolicy() {
        return milestoneTransitionPolicy;
    }

    @Override
    public MilestoneState currentState(MilestoneEntity entity) {
        return entity.getState();
    }
}

