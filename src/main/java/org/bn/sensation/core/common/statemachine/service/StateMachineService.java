package org.bn.sensation.core.common.statemachine.service;

import org.bn.sensation.core.common.statemachine.BaseEvent;

import jakarta.validation.constraints.NotNull;

public interface StateMachineService<E extends BaseEvent> {

    void sendEvent(@NotNull Long entityId, E event);
    
}
