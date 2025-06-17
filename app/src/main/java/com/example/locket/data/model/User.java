package com.example.locket.data.model;

public class User {
    private String id;
    private String username;
    private String email;
    private String profilePicture;

    public User(String id, String username, String email, String profilePicture) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getProfilePicture() { return profilePicture; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
} 