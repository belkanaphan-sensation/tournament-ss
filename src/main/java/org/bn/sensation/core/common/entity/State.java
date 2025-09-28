package org.bn.sensation.core.common.entity;

import java.util.List;

public enum State {
    DRAFT,
    WAITING,
    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    public static List<State> LIFE_STATES = List.of(WAITING, PLANNED, IN_PROGRESS, COMPLETED);
}
