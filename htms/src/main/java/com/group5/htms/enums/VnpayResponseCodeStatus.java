package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum VnpayResponseCodeStatus {

    SUCCESS("00"),
    ORDER_NOT_FOUND("01"),
    ORDER_ALREADY_CONFIRMED("02"),
    INVALID_AMOUNT("04"),
    INVALID_SIGNATURE("97");

    private final String value;

    VnpayResponseCodeStatus(String value) {
        this.value = value;
    }
}