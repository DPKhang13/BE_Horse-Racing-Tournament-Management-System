package com.group5.htms.enums;

import lombok.Getter;

@Getter
public enum RoleType {

    ADMIN("admin"),
    HORSE_OWNER("horse_owner"),
    JOCKEY("jockey"),
    RACE_REFEREE("race_referee"),
    SPECTATOR("spectator");

    private final String value;

    RoleType(String value) {
        this.value = value;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public static RoleType fromValue(String value) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.value.equalsIgnoreCase(value)) {
                return roleType;
            }
        }

        throw new IllegalArgumentException("Invalid role type: " + value);
    }
}