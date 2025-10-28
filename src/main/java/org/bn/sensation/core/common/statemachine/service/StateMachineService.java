package org.bn.sensation.core.common.statemachine.service;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.statemachine.BaseEvent;

public interface StateMachineService<T extends BaseEntity, E extends BaseEvent> {

    void sendEvent(T entity, E event);

}
