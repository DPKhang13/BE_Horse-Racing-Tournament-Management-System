package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum SoftDeleteStatus {
    DELETED("deleted");

    private final String value;

    SoftDeleteStatus(String value) {
        this.value = value;
    }
}
