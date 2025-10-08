package org.bn.sensation.core.common.statemachine.state;

import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum RoundState implements BaseState {
    DRAFT,
    PLANNED,
    IN_PROGRESS,
    ACCEPTED,
    COMPLETED;

    public static List<RoundState> LIFE_ROUND_STATES = List.of(PLANNED, IN_PROGRESS, ACCEPTED, COMPLETED);
}
