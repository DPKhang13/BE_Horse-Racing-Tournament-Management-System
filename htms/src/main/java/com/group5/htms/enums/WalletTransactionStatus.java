package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum WalletTransactionStatus {

    PENDING("pending"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    private final String value;

    WalletTransactionStatus(String value) {
        this.value = value;
    }
}