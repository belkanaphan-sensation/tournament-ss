package org.bn.sensation.core.user.entity;

public enum UserActivityRole {
    JUDGE_CHIEF,
    JUDGE,
    PARTICIPANT,
    ORGANIZER,
    OBSERVER;

    public boolean isJudgeRole() {
        return this == JUDGE_CHIEF || this == JUDGE;
    }

    public boolean isChiefJudge() {
        return this == JUDGE_CHIEF;
    }
}
