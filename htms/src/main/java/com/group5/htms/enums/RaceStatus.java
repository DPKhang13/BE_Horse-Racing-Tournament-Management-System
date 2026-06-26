package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RaceStatus {
    SCHEDULED("scheduled"),
    UPCOMING("upcoming"),
    ONGOING("ongoing"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    RaceStatus(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (RaceStatus status : values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }
}
