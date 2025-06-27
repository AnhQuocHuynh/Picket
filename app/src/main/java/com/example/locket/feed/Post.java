package com.example.locket.feed;

public class Post {
    private final String imageUrl;
    private final String caption;
    private final String userAvatarUrl;
    private final String username;
    private final String postTime;

    public Post(String imageUrl, String caption, String userAvatarUrl, String username, String postTime) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.userAvatarUrl = userAvatarUrl;
        this.username = username;
        this.postTime = postTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPostTime() {
        return postTime;
    }
}
