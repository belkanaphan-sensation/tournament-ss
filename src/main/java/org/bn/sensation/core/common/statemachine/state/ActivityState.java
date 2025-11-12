package org.bn.sensation.core.common.statemachine.state;

import java.util.Arrays;
import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum ActivityState implements BaseState {

    PLANNED,
    REGISTRATION_CLOSED,
    IN_PROGRESS,
    SUMMARIZING,
    COMPLETED;

    public static List<ActivityState> LIFE_ACTIVITY_STATES = Arrays.stream(ActivityState.values()).toList();
}
