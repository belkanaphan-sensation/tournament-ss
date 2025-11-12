package org.bn.sensation.core.common.statemachine.policy;

import java.util.Set;

import org.bn.sensation.core.common.statemachine.BaseEvent;
import org.bn.sensation.core.user.entity.Role;

public interface TransitionPolicy<E extends BaseEvent> {

    Set<E> allowedEvents(Role role);

    default boolean isAllowed(Role role, E event) {
        return allowedEvents(role).contains(event);
    }
}

