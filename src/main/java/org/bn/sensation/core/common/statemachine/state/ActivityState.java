package org.bn.sensation.core.common.statemachine.state;

import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum ActivityState implements BaseState {
    DRAFT,
    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    public static List<ActivityState> LIFE_ACTIVITY_STATES = List.of(PLANNED, IN_PROGRESS, COMPLETED);
}
