package com.example.locket.common.models.friendship;

public class AcceptLinkRequest {
    private String token;

    public AcceptLinkRequest() {}

    public AcceptLinkRequest(String token) {
        this.token = token;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
} 