package org.bn.sensation.core.common.entity;

import java.util.List;

public enum State {
    DRAFT,
    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    static List<State> LIFE_STATES = List.of(PLANNED, IN_PROGRESS, COMPLETED);
}
