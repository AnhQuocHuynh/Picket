package com.example.locket.common.models.friendship;

import java.io.Serializable;

public class FriendshipResponse implements Serializable {
    private boolean success;
    private String message;
    private FriendshipData data;

    public FriendshipResponse() {}

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

    public FriendshipData getData() {
        return data;
    }

    public void setData(FriendshipData data) {
        this.data = data;
    }
} 