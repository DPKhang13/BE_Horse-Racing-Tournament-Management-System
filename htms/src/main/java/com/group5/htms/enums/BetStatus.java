package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum BetStatus {

    PENDING("pending"),
    WON("won"),
    LOST("lost"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    DELETED("deleted");

    private final String value;

    BetStatus(String value) {
        this.value = value;
    }
}
