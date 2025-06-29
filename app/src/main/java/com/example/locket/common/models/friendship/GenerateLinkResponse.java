package com.example.locket.common.models.friendship;

import java.io.Serializable;

public class GenerateLinkResponse implements Serializable {
    private boolean success;
    private String link;

    public GenerateLinkResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
} 