package org.bn.sensation.core.common.statemachine.policy;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.BaseEvent;
import org.bn.sensation.core.common.statemachine.BaseState;

public interface TransitionSupport<
        T extends BaseEntity,
        S extends BaseState,
        E extends BaseEvent> {

    Class<T> entityType();

    BaseStateService<T, S, E> stateService();

    TransitionPolicy<E> transitionPolicy();

    S currentState(T entity);
}
