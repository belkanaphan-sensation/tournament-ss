package org.bn.sensation.core.user.entity;

public enum UserActivityRole {
    JUDGE_CHIEF("Главный судья"),
    JUDGE("Судья"),
    PARTICIPANT("Участник"),
    ORGANIZER("Организатор"),
    OBSERVER("Наблюдатель");

    private final String description;

    UserActivityRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isJudgeRole() {
        return this == JUDGE_CHIEF || this == JUDGE;
    }

    public boolean isChiefJudge() {
        return this == JUDGE_CHIEF;
    }
}
