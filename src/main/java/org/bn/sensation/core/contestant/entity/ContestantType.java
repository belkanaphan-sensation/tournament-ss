package org.bn.sensation.core.contestant.entity;

public enum ContestantType {
    SINGLE(1, true, true, "SINGLE"),
    COUPLE_TRANSIENT(2, false, false, "COUPLE"),
    COUPLE_PERSISTENT(2, true, false, "COUPLE");

    private int participantCount;
    private boolean isPersistent;
    private boolean hasPartnerSide;
    private String contestantType;

    ContestantType(int participantCount, boolean isPersistent, boolean hasPartnerSide, String contestantType) {
        this.participantCount = participantCount;
        this.isPersistent = isPersistent;
        this.hasPartnerSide = hasPartnerSide;
        this.contestantType = contestantType;
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

    public String getContestantType() {
        return contestantType;
    }
}
