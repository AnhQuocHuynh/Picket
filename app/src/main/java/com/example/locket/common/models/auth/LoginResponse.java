package com.example.locket.common.models.auth;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    private boolean success;
    private String message;
    private String token;
    private String refreshToken;
    private long expiresIn;
    private UserData user;
    
    // Additional fields for backward compatibility với LoginRespone
    private String localId;
    private String profilePicture;
    private String displayName;
    private String email;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String token, String refreshToken, long expiresIn, UserData user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Alias methods for backward compatibility với old code
    public String getIdToken() {
        return token;  // idToken và token là như nhau
    }

    public void setIdToken(String idToken) {
        this.token = idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    // Additional methods for backward compatibility với LoginRespone
    public String getLocalId() {
        return localId != null ? localId : (user != null ? user.getId() : null);
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getProfilePicture() {
        return profilePicture != null ? profilePicture : (user != null ? user.getProfilePicture() : null);
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : (user != null ? user.getDisplayName() : null);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email != null ? email : (user != null ? user.getEmail() : null);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Nested UserData class
    public static class UserData implements Serializable {
        private String id;
        private String email;
        private String username;
        private String displayName;
        private String profilePicture;

        public UserData() {}

        public UserData(String id, String email, String username, String displayName, String profilePicture) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.displayName = displayName;
            this.profilePicture = profilePicture;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }
    }
} 