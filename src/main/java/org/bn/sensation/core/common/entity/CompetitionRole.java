package org.bn.sensation.core.common.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompetitionRole {
    LEADER("Лидер"),
    FOLLOWER("Ведомый");

    private final String displayName;
}
