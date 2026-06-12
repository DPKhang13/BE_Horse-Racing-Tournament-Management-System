package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum UserStatus {

    ACTIVE("active"),
    INACTIVE("inactive"),
    BANNED("banned");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public static UserStatus fromValue(String value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid user status: " + value);
    }
}