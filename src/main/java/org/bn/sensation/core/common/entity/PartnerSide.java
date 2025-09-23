package org.bn.sensation.core.common.entity;

public enum PartnerSide {
    LEADER,
    FOLLOWER;

    static PartnerSide getOppositeSide(PartnerSide role) {
        return role == LEADER ? FOLLOWER : LEADER;
    }
}
