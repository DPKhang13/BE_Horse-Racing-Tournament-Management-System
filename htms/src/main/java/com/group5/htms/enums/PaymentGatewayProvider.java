package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum PaymentGatewayProvider {

    VNPAY("vnpay"),
    SEPAY("sepay");

    private final String value;

    PaymentGatewayProvider(String value) {
        this.value = value;
    }
}