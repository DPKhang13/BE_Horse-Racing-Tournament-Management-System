package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum WalletTransactionType {

    TOPUP("topup"),
    BET("bet"),
    REWARD("reward"),
    REFUND("refund");

    private final String value;

    WalletTransactionType(String value) {
        this.value = value;
    }
}