package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum PrizeAwardStatus {

    ANNOUNCED("announced"),
    AWARDED("awarded");

    private final String value;

    PrizeAwardStatus(String value) {
        this.value = value;
    }
}
