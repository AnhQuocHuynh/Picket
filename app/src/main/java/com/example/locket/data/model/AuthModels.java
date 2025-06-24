package com.example.locket.data.model;

public class AuthModels {
    
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String fullName;

        public RegisterRequest(String username, String email, String password, String fullName) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.fullName = fullName;
        }

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
    }

    public static class AuthResponse {
        private boolean success;
        private String message;
        private AuthData data;

        public AuthResponse(boolean success, String message, AuthData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public AuthData getData() { return data; }
    }

    public static class AuthData {
        private String token;
        private User user;

        public AuthData(String token, User user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public User getUser() { return user; }
    }
} 