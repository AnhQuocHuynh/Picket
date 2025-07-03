package com.example.locket.common.models.auth;

public class ResendVerificationRequest {
    private String email;

    public ResendVerificationRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
} 