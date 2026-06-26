package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum HorseStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    RETIRED("retired");

    private final String value;

    HorseStatus(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        for (HorseStatus status : values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }

        return false;
    }
}
