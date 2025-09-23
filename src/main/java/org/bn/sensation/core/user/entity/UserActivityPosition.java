package org.bn.sensation.core.user.entity;

public enum UserActivityPosition {
    JUDGE_CHIEF,
    JUDGE,
    PARTICIPANT,
    ORGANIZER,
    OBSERVER;

    public boolean isJudge() {
        return this == JUDGE_CHIEF || this == JUDGE;
    }

    public boolean isChiefJudge() {
        return this == JUDGE_CHIEF;
    }
}
