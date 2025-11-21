package org.bn.sensation.core.contestant.entity;

public enum ContestantType {
    SINGLE(1, true, true), COUPLE_TRANSIENT(2, false, false), COUPLE_PERSISTENT(2, true, false);

    private int participantCount;
    private boolean isPersistent;
    private boolean hasPartnerSide;

    ContestantType(int participantCount, boolean isPersistent, boolean hasPartnerSide) {
        this.participantCount = participantCount;
        this.isPersistent = isPersistent;
        this.hasPartnerSide = hasPartnerSide;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public boolean hasPartnerSide() {
        return hasPartnerSide;
    }
}
