package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RoleStatus {

    ACTIVE("active"),
    SUSPENDED("suspended"),
    BANNED("banned");

    private final String value;

    RoleStatus(String value) {
        this.value = value;
    }

    public static RoleStatus fromValue(String value) {
        for (RoleStatus status : RoleStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid role status: " + value);
    }
}