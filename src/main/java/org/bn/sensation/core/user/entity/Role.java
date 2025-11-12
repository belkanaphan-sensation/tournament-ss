package org.bn.sensation.core.user.entity;

public enum Role {
    SUPERADMIN,
    ADMIN,
    MANAGER,
    ADMINISTRATOR,
    USER,
    ANNOUNCER,
    READER;

    public boolean isAdmin() {
        return this == ADMIN || this == SUPERADMIN;
    }

}
