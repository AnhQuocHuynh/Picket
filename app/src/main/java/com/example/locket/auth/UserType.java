package com.example.locket.auth;

public enum UserType {
    FREE("free"),
    PRO("pro");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserType fromString(String text) {
        for (UserType type : UserType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return FREE; // Default to FREE
    }
}