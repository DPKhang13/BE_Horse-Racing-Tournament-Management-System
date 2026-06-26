package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum JockeyAssignmentStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    DELETED("deleted");

    private final String value;

    JockeyAssignmentStatus(String value) {
        this.value = value;
    }
}
