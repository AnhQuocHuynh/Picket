package com.example.locket.common.models.post;

public class CommentRequest {
    private String text;

    public CommentRequest() {}

    public CommentRequest(String text) {
        this.text = text;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
} 