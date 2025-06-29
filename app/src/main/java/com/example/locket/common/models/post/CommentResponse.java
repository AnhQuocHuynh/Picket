package com.example.locket.common.models.post;

import java.io.Serializable;

public class CommentResponse implements Serializable {
    private boolean success;
    private String message;
    private Post.Comment comment;

    public CommentResponse() {}

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

    public Post.Comment getComment() {
        return comment;
    }

    public void setComment(Post.Comment comment) {
        this.comment = comment;
    }
} 