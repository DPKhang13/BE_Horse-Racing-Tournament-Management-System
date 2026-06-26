package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RaceRegistrationStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    DELETED("deleted");

    private final String value;

    RaceRegistrationStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String value) {
        return this.value.equalsIgnoreCase(clean(value));
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (RaceRegistrationStatus status : RaceRegistrationStatus.values()) {
            if (status.equalsValue(value)) {
                return true;
            }
        }

        return false;
    }

    private static String clean(String status) {
        return status == null ? null : status.trim().toLowerCase();
    }
}
