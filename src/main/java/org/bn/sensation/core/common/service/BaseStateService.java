package org.bn.sensation.core.common.service;

import java.util.Optional;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.statemachine.BaseEvent;
import org.bn.sensation.core.common.statemachine.BaseState;

public interface BaseStateService <T extends BaseEntity, S extends BaseState, E extends BaseEvent> {

    void saveTransition(T entity, S state);

    /**
     * @return null when transition allowed, otherwise human-readable reason why it is blocked
     */
    String canTransition(T entity, E event);

    Optional<S> getNextState(S currentState, E event);
}
