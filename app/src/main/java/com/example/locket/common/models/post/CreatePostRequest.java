package com.example.locket.common.models.post;

public class CreatePostRequest {
    private String imageUrl;
    private String caption;

    public CreatePostRequest() {}

    public CreatePostRequest(String imageUrl, String caption) {
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    // Getters and Setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
} 