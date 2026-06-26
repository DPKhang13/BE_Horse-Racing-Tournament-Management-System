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
}
