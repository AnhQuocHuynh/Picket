package com.example.locket.common.models.post;

import java.io.Serializable;

public class PostResponse implements Serializable {
    private boolean success;
    private String message;
    private Post data;

    public PostResponse() {}

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

    public Post getData() {
        return data;
    }

    public void setData(Post data) {
        this.data = data;
    }
} 