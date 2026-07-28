package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RaceResultStatus {
    DRAFT("draft"),
    CONFIRMED("confirmed"),
    PUBLISHED("published"),
    CANCELLED("cancelled");

    private final String value;

    RaceResultStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String value) {
        return this.value.equalsIgnoreCase(clean(value));
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (RaceResultStatus status : RaceResultStatus.values()) {
            if (status.equalsValue(value)) {
                return true;
            }
        }

        return false;
    }

    private static String clean(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
