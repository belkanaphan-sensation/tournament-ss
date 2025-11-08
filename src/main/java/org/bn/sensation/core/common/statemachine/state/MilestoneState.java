package org.bn.sensation.core.common.statemachine.state;

import java.util.Arrays;
import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum MilestoneState implements BaseState {
    DRAFT,
    PLANNED,
    PENDING,
    IN_PROGRESS,
    SUMMARIZING,
    COMPLETED,
    SKIPPED;

    public static List<MilestoneState> LIFE_MILESTONE_STATES = Arrays.stream(MilestoneState.values())
            .filter(s -> s != DRAFT)
            .toList();
}
