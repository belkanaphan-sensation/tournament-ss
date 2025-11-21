package org.bn.sensation.core.round.service.policy;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bn.sensation.core.round.statemachine.RoundEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoundTransitionPolicy implements TransitionPolicy<RoundEvent> {

    private final Map<Role, Set<RoundEvent>> rolePermissions;

    public RoundTransitionPolicy() {
        EnumMap<Role, Set<RoundEvent>> map = new EnumMap<>(Role.class);
        Set<RoundEvent> managerEvents = EnumSet.allOf(RoundEvent.class);

        map.put(Role.SUPERADMIN, managerEvents);
        map.put(Role.ADMIN, managerEvents);
        map.put(Role.MANAGER, managerEvents);

        this.rolePermissions = Collections.unmodifiableMap(map);
    }

    @Override
    public Set<RoundEvent> allowedEvents(Role role) {
        return rolePermissions.getOrDefault(role, Collections.emptySet());
    }
}
