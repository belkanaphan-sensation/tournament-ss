package org.bn.sensation.core.occasion.service.policy;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bn.sensation.core.occasion.statemachine.OccasionEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class OccasionTransitionPolicy implements TransitionPolicy<OccasionEvent> {

    private final Map<Role, Set<OccasionEvent>> rolePermissions;

    public OccasionTransitionPolicy() {
        EnumMap<Role, Set<OccasionEvent>> map = new EnumMap<>(Role.class);
        Set<OccasionEvent> managerEvents = EnumSet.allOf(OccasionEvent.class);

        map.put(Role.SUPERADMIN, managerEvents);
        map.put(Role.ADMIN, managerEvents);
        map.put(Role.MANAGER, managerEvents);

        this.rolePermissions = Collections.unmodifiableMap(map);
    }

    @Override
    public Set<OccasionEvent> allowedEvents(Role role) {
        return rolePermissions.getOrDefault(role, Collections.emptySet());
    }
}
