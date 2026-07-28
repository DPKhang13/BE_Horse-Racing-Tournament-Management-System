package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum TournamentStatus {

    UPCOMING("upcoming"),
    REGISTRATION_OPEN("registration_open"),
    REGISTRATION_CLOSED("registration_closed"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    TournamentStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String value) {
        return this.value.equalsIgnoreCase(clean(value));
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (TournamentStatus status : TournamentStatus.values()) {
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
