package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum ChiefInspectionStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    ChiefInspectionStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String value) {
        return this.value.equalsIgnoreCase(clean(value));
    }

    private static String clean(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
