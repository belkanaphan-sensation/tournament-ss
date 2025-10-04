package org.bn.sensation.core.common.statemachine.state;

import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum OccasionState implements BaseState {

    DRAFT,
    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    public static List<OccasionState> LIFE_OCCASION_STATES = List.of(PLANNED, IN_PROGRESS, COMPLETED);
}
