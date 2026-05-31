package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum WalletStatus {

    ACTIVE("active"),
    SUSPENDED("suspended"),
    CLOSED("closed");

    private final String value;

    WalletStatus(String value) {
        this.value = value;
    }
}