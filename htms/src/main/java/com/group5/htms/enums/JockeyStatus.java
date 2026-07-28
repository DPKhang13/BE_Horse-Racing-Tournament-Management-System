package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum JockeyStatus {
    AVAILABLE("available"),
    UNAVAILABLE("unavailable"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    BANNED("banned");

    private final String value;

    JockeyStatus(String value) {
        this.value = value;
    }
}
