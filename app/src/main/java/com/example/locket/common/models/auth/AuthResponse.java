package com.example.locket.common.models.auth;

import java.io.Serializable;

public class AuthResponse implements Serializable {
    private boolean success;
    private String message;
    private String idToken;
    private String refreshToken;
    private String token;
    private Object data;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getIdToken() {
        return idToken != null ? idToken : token;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
        this.token = idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token != null ? token : idToken;
    }

    public void setToken(String token) {
        this.token = token;
        this.idToken = token;
    }
}
