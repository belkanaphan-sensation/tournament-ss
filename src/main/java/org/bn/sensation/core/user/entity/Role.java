package org.bn.sensation.core.user.entity;

public enum Role {
    SUPERADMIN,
    ADMIN,
    MANAGER,
    ORGANIZER,
    ADMINISTRATOR,
    USER,
    READER;

    public boolean isAdmin() {
        return this == ADMIN || this == SUPERADMIN;
    }

}
