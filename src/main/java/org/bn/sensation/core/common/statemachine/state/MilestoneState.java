package org.bn.sensation.core.common.statemachine.state;

import java.util.List;

import org.bn.sensation.core.common.statemachine.BaseState;

public enum MilestoneState implements BaseState {
    DRAFT,
    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    public static List<MilestoneState> LIFE_MILESTONE_STATES = List.of(PLANNED, IN_PROGRESS, COMPLETED);
}
