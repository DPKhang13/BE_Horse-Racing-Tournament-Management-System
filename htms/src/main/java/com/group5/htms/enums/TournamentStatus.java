package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum TournamentStatus {

    UPCOMING("upcoming"),
    ONGOING("ongoing"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    TournamentStatus(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (TournamentStatus status : TournamentStatus.values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }
}