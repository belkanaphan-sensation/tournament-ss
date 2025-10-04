package org.bn.sensation.core.user.entity;

public enum Role {
    SUPERADMIN,
    ADMIN,
    OCCASION_ADMIN, //TODO may be delete from here
    USER,
    READER;

    public boolean isAdmin() {
        return this == ADMIN || this == SUPERADMIN;
    }

}
