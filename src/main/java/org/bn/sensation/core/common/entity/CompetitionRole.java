package org.bn.sensation.core.common.entity;

public enum CompetitionRole {
    LEADER,
    FOLLOWER;

    static CompetitionRole getOppositeRole(CompetitionRole role) {
        return role == LEADER ? FOLLOWER : LEADER;
    }
}
