package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RaceResultStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    DELETED("deleted");

    private final String value;

    RaceResultStatus(String value) {
        this.value = value;
    }
}
