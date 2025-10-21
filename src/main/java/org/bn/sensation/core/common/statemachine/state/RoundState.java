package org.bn.sensation.core.common.statemachine.state;

import java.util.Arrays;
import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum RoundState implements BaseState {
    DRAFT,
    PLANNED,
    IN_PROGRESS,
    READY,
    COMPLETED;

    public static List<RoundState> LIFE_ROUND_STATES = Arrays.stream(RoundState.values())
            .filter(s -> s != DRAFT)
            .toList();
}
