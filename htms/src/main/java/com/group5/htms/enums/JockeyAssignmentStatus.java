package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum JockeyAssignmentStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    EXPIRED("expired"),
    CONFIRMED("confirmed"),
    DELETED("deleted");

    private final String value;

    JockeyAssignmentStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String value) {
        return this.value.equalsIgnoreCase(clean(value));
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (JockeyAssignmentStatus status : JockeyAssignmentStatus.values()) {
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
