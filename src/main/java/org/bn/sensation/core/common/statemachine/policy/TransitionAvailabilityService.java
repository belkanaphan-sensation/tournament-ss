package org.bn.sensation.core.common.statemachine.policy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.statemachine.BaseEvent;
import org.bn.sensation.core.common.statemachine.BaseState;
import org.bn.sensation.core.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class TransitionAvailabilityService {

    private final Map<Class<? extends BaseEntity>, TransitionSupport<?, ?, ?>> supportByEntity;

    public TransitionAvailabilityService(List<TransitionSupport<?, ?, ?>> supports) {
        Map<Class<? extends BaseEntity>, TransitionSupport<?, ?, ?>> map = new HashMap<>();
        for (TransitionSupport<?, ?, ?> support : supports) {
            map.put(support.entityType(), support);
        }
        this.supportByEntity = Collections.unmodifiableMap(map);
    }

    public <T extends BaseEntity, S extends BaseState, E extends BaseEvent> Set<E> findAllowedStates(T entity, Role role) {
        TransitionSupport<T, S, E> support = findSupport(entity);
        if (support == null) {
            return Collections.emptySet();
        }

        S currentState = support.currentState(entity);
        Set<E> allowedEvents = new HashSet<>();
        Set<E> allowedEventsForRole = support.transitionPolicy().allowedEvents(role);

        for (E event : allowedEventsForRole) {
            Optional<S> maybeNextState = support.stateService().getNextState(currentState, event);
            if (maybeNextState.isEmpty()) {
                continue;
            }

            S nextState = maybeNextState.get();
            if (nextState.equals(currentState)) {
                continue;
            }

            String validationError = support.stateService().canTransition(entity, event);
            if (validationError == null) {
                allowedEvents.add(event);
            }
        }

        return allowedEvents;
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseEntity, S extends BaseState, E extends BaseEvent> TransitionSupport<T, S, E> findSupport(T entity) {
        return (TransitionSupport<T, S, E>) supportByEntity.get(entity.getClass());
    }
}

