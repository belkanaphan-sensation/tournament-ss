package org.bn.sensation.core.occasion.statemachine;

import java.util.Arrays;
import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum OccasionState implements BaseState {

    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    public static List<OccasionState> LIFE_OCCASION_STATES = Arrays.stream(OccasionState.values()).toList();
}
