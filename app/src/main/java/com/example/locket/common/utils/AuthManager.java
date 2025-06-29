package com.example.locket.common.utils;

import android.content.Context;
import android.util.Log;

import com.example.locket.common.models.auth.AuthResponse;
import com.example.locket.common.models.auth.LoginRequest;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.auth.RegisterRequest;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.models.common.ApiResponse;
import com.example.locket.common.network.AuthApiService;
import com.example.locket.common.network.client.AuthApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private static AuthApiService authApiService;

    // Initialize API service
    private static AuthApiService getAuthApiService() {
        if (authApiService == null) {
            authApiService = AuthApiClient.getAuthClient().create(AuthApiService.class);
        }
        return authApiService;
    }

    // ==================== AUTHENTICATION CALLBACKS ====================
    
    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String errorMessage, int errorCode);
        void onLoading(boolean isLoading);
    }

    public interface LoginCallback extends AuthCallback {
        void onLoginSuccess(LoginResponse loginResponse);
    }

    public interface RegisterCallback extends AuthCallback {
        void onRegisterSuccess(AuthResponse authResponse);
    }

    public interface ProfileCallback {
        void onSuccess(UserProfile userProfile);
        void onError(String errorMessage, int errorCode);
    }

    // ==================== AUTHENTICATION METHODS ====================

    /**
     * 🔑 USER LOGIN
     */
    public static void login(Context context, String email, String password, LoginCallback callback) {
        if (callback != null) callback.onLoading(true);
        
        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = getAuthApiService().login(request);
        
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        // Save token and user data
                        SharedPreferencesUser.saveJWTToken(context, loginResponse.getToken());
                        SharedPreferencesUser.saveLoginResponse(context, loginResponse);
                        
                        Log.d(TAG, "Login successful for user: " + loginResponse.getEmail());
                        if (callback != null) {
                            callback.onLoginSuccess(loginResponse);
                            callback.onSuccess("Login successful!");
                        }
                    } else {
                        String errorMsg = loginResponse.getMessage() != null ? 
                            loginResponse.getMessage() : "Login failed";
                        Log.e(TAG, "Login failed: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg, 401);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    Log.e(TAG, "Login error: " + errorMsg + " (Code: " + response.code() + ")");
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 🔐 USER REGISTRATION
     */
    public static void register(Context context, String username, String email, String password, 
                               RegisterCallback callback) {
        if (callback != null) callback.onLoading(true);
        
        RegisterRequest request = new RegisterRequest(username, email, password);
        
        // 🔍 Debug logging
        Log.d(TAG, "🚀 Register request data:");
        Log.d(TAG, "   Username: " + username);
        Log.d(TAG, "   Email: " + email);
        Log.d(TAG, "   Password: " + (password != null ? "***" + password.length() + "***" : "null"));
        Log.d(TAG, "   Request object: " + request.toString());
        
        Call<AuthResponse> call = getAuthApiService().register(request);
        
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        // Auto-save token if provided in registration response
                        if (authResponse.getToken() != null) {
                            SharedPreferencesUser.saveJWTToken(context, authResponse.getToken());
                        }
                        
                        Log.d(TAG, "Registration successful");
                        if (callback != null) {
                            callback.onRegisterSuccess(authResponse);
                            callback.onSuccess("Registration successful!");
                        }
                    } else {
                        String errorMsg = authResponse.getMessage() != null ? 
                            authResponse.getMessage() : "Registration failed";
                        Log.e(TAG, "Registration failed: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg, 400);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    try {
                        if(response.errorBody()!=null){
                            errorMsg += " | Body: " + response.errorBody().string();
                        }
                    }catch(Exception ex){
                        Log.e(TAG,"Error reading errorBody",ex);
                    }
                    Log.e(TAG, "Registration error: " + errorMsg + " (Code: " + response.code() + ")");
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 👤 GET USER PROFILE
     */
    public static void getUserProfile(Context context, ProfileCallback callback) {
        String token = SharedPreferencesUser.getJWTToken(context);
        if (token == null || token.isEmpty()) {
            if (callback != null) callback.onError("No authentication token found", 401);
            return;
        }
        
        Call<UserProfile> call = getAuthApiService().getUserProfile("Bearer " + token);
        
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile userProfile = response.body();
                    Log.d(TAG, "Profile retrieved successfully");
                    if (callback != null) callback.onSuccess(userProfile);
                } else {
                    String errorMsg = getErrorMessage(response);
                    Log.e(TAG, "Get profile error: " + errorMsg + " (Code: " + response.code() + ")");
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 🚪 LOGOUT
     */
    public static void logout(Context context, AuthCallback callback) {
        String token = SharedPreferencesUser.getJWTToken(context);
        
        if (token != null && !token.isEmpty()) {
            Call<ApiResponse> call = getAuthApiService().logout("Bearer " + token);
            
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    // Clear local data regardless of API response
                    SharedPreferencesUser.clearAll(context);
                    
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Logout successful");
                        if (callback != null) callback.onSuccess("Logged out successfully");
                    } else {
                        Log.w(TAG, "Logout API failed but local data cleared");
                        if (callback != null) callback.onSuccess("Logged out (local data cleared)");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Clear local data even if network call fails
                    SharedPreferencesUser.clearAll(context);
                    Log.w(TAG, "Logout network error but local data cleared: " + t.getMessage());
                    if (callback != null) callback.onSuccess("Logged out (offline)");
                }
            });
        } else {
            // No token, just clear local data
            SharedPreferencesUser.clearAll(context);
            Log.d(TAG, "Logout - no token, cleared local data");
            if (callback != null) callback.onSuccess("Logged out");
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if user is logged in (has valid token)
     */
    public static boolean isLoggedIn(Context context) {
        String token = SharedPreferencesUser.getJWTToken(context);
        return token != null && !token.isEmpty();
    }

    /**
     * Get authorization header for API calls
     */
    public static String getAuthHeader(Context context) {
        String token = SharedPreferencesUser.getJWTToken(context);
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Extract error message from response
     */
    private static String getErrorMessage(Response<?> response) {
        switch (response.code()) {
            case 400:
                return "Invalid request data";
            case 401:
                return "Invalid credentials or session expired";
            case 403:
                return "Access forbidden";
            case 404:
                return "Resource not found";
            case 500:
                return "Server error - please try again later";
            default:
                return "Unknown error occurred (Code: " + response.code() + ")";
        }
    }
} 