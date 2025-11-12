package org.bn.sensation.core.milestone.service.policy;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.policy.TransitionPolicy;
import org.bn.sensation.core.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class MilestoneTransitionPolicy implements TransitionPolicy<MilestoneEvent> {

    private final Map<Role, Set<MilestoneEvent>> rolePermissions;

    public MilestoneTransitionPolicy() {
        EnumMap<Role, Set<MilestoneEvent>> map = new EnumMap<>(Role.class);
        Set<MilestoneEvent> managerEvents = EnumSet.allOf(MilestoneEvent.class);

        map.put(Role.SUPERADMIN, managerEvents);
        map.put(Role.ADMIN, managerEvents);
        map.put(Role.MANAGER, managerEvents);

        this.rolePermissions = Collections.unmodifiableMap(map);
    }

    @Override
    public Set<MilestoneEvent> allowedEvents(Role role) {
        return rolePermissions.getOrDefault(role, Collections.emptySet());
    }
}

