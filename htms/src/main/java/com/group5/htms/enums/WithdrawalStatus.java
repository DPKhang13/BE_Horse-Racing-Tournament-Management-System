package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum WithdrawalStatus {

    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    PAID("paid");

    private final String value;

    WithdrawalStatus(String value) {
        this.value = value;
    }

    public boolean equalsValue(String status) {
        return status != null && this.value.equalsIgnoreCase(status);
    }
}