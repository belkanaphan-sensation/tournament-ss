package org.bn.sensation.core.common.statemachine.state;

import java.util.Arrays;
import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum ActivityState implements BaseState {
    DRAFT,
    PLANNED,
    REGISTRATION_CLOSED,
    IN_PROGRESS,
    COMPLETED;

    public static List<ActivityState> LIFE_ACTIVITY_STATES = Arrays.stream(ActivityState.values())
            .filter(s -> s != DRAFT)
            .toList();
}
