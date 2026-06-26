package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RaceStatus {
    SCHEDULED("scheduled"),
    UPCOMING("upcoming"),
    ONGOING("ongoing"),
    REGISTRATION_OPEN("registration_open"),
    REGISTRATION_CLOSED("registration_closed"),
    READY("ready"),
    OPEN_FOR_BETTING("open_for_betting"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    RaceStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String value) {
        return this.value.equalsIgnoreCase(clean(value));
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (RaceStatus status : RaceStatus.values()) {
            if (status.equalsValue(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean canOpenRegistration(String status) {
        return SCHEDULED.equalsValue(status) || UPCOMING.equalsValue(status);
    }

    public static boolean canOpenBetting(String status) {
        return READY.equalsValue(status);
    }

    public static boolean canStartRace(String status) {
        return canStart(status);
    }

    public static boolean canStart(String status) {
        return READY.equalsValue(status) || OPEN_FOR_BETTING.equalsValue(status);
    }

    public static boolean isBettingOpen(String status) {
        return OPEN_FOR_BETTING.equalsValue(status);
    }

    public static boolean canCompleteRace(String status) {
        return IN_PROGRESS.equalsValue(status);
    }

    public static boolean canCancel(String status) {
        return status != null
                && !COMPLETED.equalsValue(status)
                && !CANCELLED.equalsValue(status);
    }

    private static String clean(String status) {
        return status == null ? null : status.trim().toLowerCase();
    }
}
