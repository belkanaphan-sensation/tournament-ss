package org.bn.sensation.core.activity.service.policy;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bn.sensation.core.activity.statemachine.ActivityEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class ActivityTransitionPolicy implements TransitionPolicy<ActivityEvent> {

    private final Map<Role, Set<ActivityEvent>> rolePermissions;

    public ActivityTransitionPolicy() {
        EnumMap<Role, Set<ActivityEvent>> map = new EnumMap<>(Role.class);
        Set<ActivityEvent> managerEvents = EnumSet.allOf(ActivityEvent.class);

        map.put(Role.SUPERADMIN, managerEvents);
        map.put(Role.ADMIN, managerEvents);
        map.put(Role.MANAGER, managerEvents);
        map.put(Role.ADMINISTRATOR, EnumSet.of(ActivityEvent.CLOSE_REGISTRATION, ActivityEvent.PLAN));

        this.rolePermissions = Collections.unmodifiableMap(map);
    }

    @Override
    public Set<ActivityEvent> allowedEvents(Role role) {
        return rolePermissions.getOrDefault(role, Collections.emptySet());
    }
}

