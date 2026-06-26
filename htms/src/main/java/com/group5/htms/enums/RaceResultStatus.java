package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RaceResultStatus {
    DRAFT("draft"),
    PUBLISHED("published");

    private final String value;

    RaceResultStatus(String value) {
        this.value = value;
    }
}
