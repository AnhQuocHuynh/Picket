package com.example.locket.common.network;

import android.util.Log;

import com.example.locket.common.models.login.check_email.CheckEmailResponse;
import com.example.locket.common.utils.MockDataService;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class MockLoginService {
    
    private static final String TAG = "MockLoginService";
    
    // Mock emails that should pass validation
    private static final String[] VALID_MOCK_EMAILS = {
        "test@example.com",
        "demo@test.com", 
        "admin@locket.com",
        "user@demo.com"
    };
    
    // Mock passwords 
    private static final String[] VALID_MOCK_PASSWORDS = {
        "123456",
        "password",
        "test123",
        "demo123"
    };
    
    /**
     * Check if email is valid for mock testing
     */
    public static boolean isValidMockEmail(String email) {
        if (email == null) return false;
        
        for (String validEmail : VALID_MOCK_EMAILS) {
            if (validEmail.equals(email.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if password is valid for mock testing
     */
    public static boolean isValidMockPassword(String password) {
        if (password == null) return false;
        
        for (String validPassword : VALID_MOCK_PASSWORDS) {
            if (validPassword.equals(password.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Mock check email API call
     */
    public static Call<CheckEmailResponse> mockCheckEmail(String email) {
        Log.d(TAG, "Mock checking email: " + email);
        
        CheckEmailResponse response = new CheckEmailResponse();
        CheckEmailResponse.Result result = new CheckEmailResponse.Result();
        
        if (isValidMockEmail(email)) {
            result.setStatus(200);
            result.setNeeds_registration(false);
            Log.d(TAG, "Mock email validation successful for: " + email);
        } else {
            result.setStatus(404);
            result.setNeeds_registration(true);
            Log.d(TAG, "Mock email validation failed for: " + email);
        }
        
        response.setResult(result);
        
        return new MockApiServer.MockCall<>(response, true, result.getStatus());
    }
    
    /**
     * Mock login API call
     */
    public static Call<ResponseBody> mockLogin(String email, String password) {
        Log.d(TAG, "Mock login attempt for: " + email);
        
        String jsonResponse;
        boolean success = false;
        int statusCode = 400;
        
        if (isValidMockEmail(email) && isValidMockPassword(password)) {
            jsonResponse = MockDataService.getMockLoginApiResponse();
            success = true;
            statusCode = 200;
            Log.d(TAG, "Mock login successful for: " + email);
        } else {
            jsonResponse = "{\n" +
                    "  \"error\": {\n" +
                    "    \"code\": 400,\n" +
                    "    \"message\": \"INVALID_PASSWORD\",\n" +
                    "    \"errors\": [\n" +
                    "      {\n" +
                    "        \"message\": \"INVALID_PASSWORD\",\n" +
                    "        \"domain\": \"global\",\n" +
                    "        \"reason\": \"invalid\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";
            Log.d(TAG, "Mock login failed for: " + email);
        }
        
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        
        return new MockApiServer.MockCall<>(responseBody, success, statusCode);
    }
    
    /**
     * Mock account info API call
     */
    public static Call<ResponseBody> mockGetAccountInfo(String token) {
        Log.d(TAG, "Mock getting account info for token: " + token);
        
        String jsonResponse = "{\n" +
                "  \"kind\": \"identitytoolkit#GetAccountInfoResponse\",\n" +
                "  \"users\": [\n" +
                "    {\n" +
                "      \"localId\": \"mock_user_12345\",\n" +
                "      \"email\": \"test@example.com\",\n" +
                "      \"emailVerified\": true,\n" +
                "      \"displayName\": \"Test User\",\n" +
                "      \"providerUserInfo\": [\n" +
                "        {\n" +
                "          \"providerId\": \"password\",\n" +
                "          \"federatedId\": \"test@example.com\",\n" +
                "          \"email\": \"test@example.com\",\n" +
                "          \"rawId\": \"test@example.com\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"photoUrl\": \"https://i.pravatar.cc/300?img=10\",\n" +
                "      \"passwordHash\": \"UkVEQUNURUQ=\",\n" +
                "      \"passwordUpdatedAt\": " + (System.currentTimeMillis() - 86400000) + ",\n" +
                "      \"validSince\": \"" + (System.currentTimeMillis() / 1000 - 86400) + "\",\n" +
                "      \"disabled\": false,\n" +
                "      \"lastLoginAt\": \"" + System.currentTimeMillis() + "\",\n" +
                "      \"createdAt\": \"" + (System.currentTimeMillis() - 2592000000L) + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        
        return new MockApiServer.MockCall<>(responseBody, true, 200);
    }
    
    /**
     * Mock fetch user info API call
     */
    public static Call<ResponseBody> mockFetchUser(String userId) {
        Log.d(TAG, "Mock fetching user info for: " + userId);
        
        String jsonResponse = "{\n" +
                "  \"result\": {\n" +
                "    \"status\": 200,\n" +
                "    \"data\": {\n" +
                "      \"uid\": \"mock_user_12345\",\n" +
                "      \"first_name\": \"Test\",\n" +
                "      \"last_name\": \"User\",\n" +
                "      \"badge\": \"🧪\",\n" +
                "      \"profile_picture_url\": \"https://i.pravatar.cc/300?img=10\",\n" +
                "      \"temp\": false,\n" +
                "      \"username\": \"testuser\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        
        return new MockApiServer.MockCall<>(responseBody, true, 200);
    }
    
    /**
     * Mock refresh token API call
     */
    public static Call<ResponseBody> mockRefreshToken(String refreshToken) {
        Log.d(TAG, "Mock refreshing token: " + refreshToken);
        
        String jsonResponse = "{\n" +
                "  \"access_token\": \"mock_access_token_new_" + System.currentTimeMillis() + "\",\n" +
                "  \"expires_in\": \"3600\",\n" +
                "  \"token_type\": \"Bearer\",\n" +
                "  \"refresh_token\": \"mock_refresh_token_new_" + System.currentTimeMillis() + "\",\n" +
                "  \"id_token\": \"mock_id_token_new_" + System.currentTimeMillis() + "\",\n" +
                "  \"user_id\": \"mock_user_12345\",\n" +
                "  \"project_id\": \"mock_project_12345\"\n" +
                "}";
        
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        
        return new MockApiServer.MockCall<>(responseBody, true, 200);
    }
    
    /**
     * Check if app is in mock mode (you can add flag or check based on build type)
     */
    public static boolean isMockMode() {
        // For testing, always return true
        // In production, you can check BuildConfig.DEBUG or a specific flag
        return true;
    }
    
    /**
     * Get supported mock credentials
     */
    public static String getMockCredentialsInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🧪 Mock Test Credentials:\n\n");
        
        info.append("📧 Valid Emails:\n");
        for (String email : VALID_MOCK_EMAILS) {
            info.append("• ").append(email).append("\n");
        }
        
        info.append("\n🔑 Valid Passwords:\n");
        for (String password : VALID_MOCK_PASSWORDS) {
            info.append("• ").append(password).append("\n");
        }
        
        info.append("\n✨ Recommended: test@example.com / 123456");
        
        return info.toString();
    }
} 