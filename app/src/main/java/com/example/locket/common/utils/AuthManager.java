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
import com.example.locket.common.models.auth.VerifyEmailRequest;
import com.example.locket.common.models.auth.ResendVerificationRequest;
import com.example.locket.common.models.auth.ChangePasswordRequest;
import com.example.locket.common.models.auth.ForgotPasswordRequest;
import com.example.locket.common.models.auth.ResetPasswordRequest;

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

                        // 🔄 Trigger widget update when user logs in successfully
                        WidgetUpdateHelper.onUserLoginSuccess(context);

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
    /**
     * ✏️ UPDATE USER PROFILE
     */
    public static void updateProfile(Context context, String username, String profilePictureUrl, ProfileCallback callback) {
        String token = getAuthHeader(context);
        if (token == null) {
            if (callback != null) callback.onError("No authentication token found", 401);
            return;
        }

        AuthApiService.UpdateProfileRequest request = new AuthApiService.UpdateProfileRequest(username, profilePictureUrl);
        Call<UserProfile> call = getAuthApiService().updateUserProfile(token, request);

        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile updatedProfile = response.body();
                    // Update cached user profile
                    SharedPreferencesUser.saveUserProfile(context, updatedProfile);
                    Log.d(TAG, "Profile updated successfully.");
                    if (callback != null) callback.onSuccess(updatedProfile);
                } else {
                    String errorMsg = getErrorMessage(response);
                    Log.e(TAG, "Update profile error: " + errorMsg + " (Code: " + response.code() + ")");
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

                    // 🔄 Clear widget cache when user logs out
                    WidgetUpdateHelper.onUserLogout(context);

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

                    // 🔄 Clear widget cache when user logs out
                    WidgetUpdateHelper.onUserLogout(context);

                    Log.w(TAG, "Logout network error but local data cleared: " + t.getMessage());
                    if (callback != null) callback.onSuccess("Logged out (offline)");
                }
            });
        } else {
            // No token, just clear local data
            SharedPreferencesUser.clearAll(context);

            // 🔄 Clear widget cache when user logs out
            WidgetUpdateHelper.onUserLogout(context);

            Log.d(TAG, "Logout - no token, cleared local data");
            if (callback != null) callback.onSuccess("Logged out");
        }
    }

    /**
     * 🔑 CHANGE PASSWORD
     */
    public static void changePassword(Context context, String currentPassword, String newPassword, String confirmPassword, AuthCallback callback) {
        if (callback != null) callback.onLoading(true);

        String token = SharedPreferencesUser.getJWTToken(context);
        if (token == null || token.isEmpty()) {
            if (callback != null) {
                callback.onLoading(false);
                callback.onError("No authentication token found.", 401);
            }
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);
        Call<ApiResponse> call = getAuthApiService().changePassword("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (callback != null) callback.onLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Password changed successfully.");
                        if (callback != null) callback.onSuccess(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Password changed successfully.");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to change password";
                        Log.e(TAG, "Change password failed: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg, response.code());
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    Log.e(TAG, "Change password error: " + errorMsg + " (Code: " + response.code() + ")");
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 📧 VERIFY EMAIL
     */
    public static void verifyEmail(Context context, String email, String code, AuthCallback callback) {
        if (callback != null) callback.onLoading(true);
        VerifyEmailRequest request = new VerifyEmailRequest(email, code);
        Call<ApiResponse> call = getAuthApiService().verifyEmail(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (callback != null) callback.onLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        if (callback != null) callback.onSuccess(apiResponse.getMessage());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Xác thực thất bại";
                        if (callback != null) callback.onError(errorMsg, 400);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 🔄 RESEND VERIFICATION CODE
     */
    public static void resendVerification(Context context, String email, AuthCallback callback) {
        if (callback != null) callback.onLoading(true);
        ResendVerificationRequest request = new ResendVerificationRequest(email);
        Call<ApiResponse> call = getAuthApiService().resendVerification(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (callback != null) callback.onLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        if (callback != null) callback.onSuccess(apiResponse.getMessage());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Gửi lại mã thất bại";
                        if (callback != null) callback.onError(errorMsg, 400);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 🔄 RESEND VERIFICATION CODE (with token)
     */
    public static void resendVerificationWithToken(Context context, String email, String token, AuthCallback callback) {
        if (callback != null) callback.onLoading(true);
        Call<ApiResponse> call = getAuthApiService().resendVerificationWithToken("Bearer " + token);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (callback != null) callback.onLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        if (callback != null) callback.onSuccess(apiResponse.getMessage());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Gửi lại mã thất bại";
                        if (callback != null) callback.onError(errorMsg, 400);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
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
    /**
     * 🔑 FORGOT PASSWORD
     * Sends a request to the server to initiate the password reset process for the given email.
     */
    public static void forgotPassword(String email, AuthCallback callback) {
        if (callback != null) callback.onLoading(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        Call<ApiResponse> call = getAuthApiService().forgotPassword(request);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (callback != null) callback.onLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Forgot password request successful for email: " + email);
                        if (callback != null) callback.onSuccess(apiResponse.getMessage());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Forgot password request failed";
                        Log.e(TAG, "Forgot password failed: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg, 400);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    Log.e(TAG, "Forgot password error: " + errorMsg + " (Code: " + response.code() + ")");
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    /**
     * 🔑 RESET PASSWORD
     * Sends the verification code and new password to the server to complete the password reset.
     */
    public static void resetPassword(String code, String newPassword, String confirmPassword, AuthCallback callback) {
        if (callback != null) callback.onLoading(true);

        ResetPasswordRequest request = new ResetPasswordRequest(code, newPassword, confirmPassword);
        Call<ApiResponse> call = getAuthApiService().resetPassword(request);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (callback != null) callback.onLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Password has been reset successfully.");
                        if (callback != null) callback.onSuccess(apiResponse.getMessage());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Password reset failed";
                        Log.e(TAG, "Password reset failed: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg, 400);
                    }
                } else {
                    String errorMsg = getErrorMessage(response);
                    Log.e(TAG, "Password reset error: " + errorMsg + " (Code: " + response.code() + ")");
                    if (callback != null) callback.onError(errorMsg, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) callback.onError(errorMsg, -1);
            }
        });
    }

    private static String getErrorMessage(Response<?> response) {
        if (response.errorBody() == null) {
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
        try {
            String errorBodyString = response.errorBody().string(); // Đọc error body

            // Cố gắng phân tích như một đối tượng JSON
            try {
                org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                if (errorJson.has("message")) {
                    String backendMessage = errorJson.getString("message");
                    if (backendMessage != null && !backendMessage.trim().isEmpty()) {
                        return backendMessage; // Đây chính là thông báo từ BE
                    }
                }
                // Nếu không có trường "message" hoặc nó rỗng, có thể bạn muốn trả về toàn bộ errorBodyString
                // nếu nó ngắn gọn, hoặc một thông báo chung hơn.
                // Ví dụ: return errorBodyString; (nếu backend trả về text đơn giản)
            } catch (org.json.JSONException e) {
                // Nếu không phải JSON hoặc lỗi phân tích, có thể errorBody là text đơn giản
                // Trả về errorBodyString nếu nó không quá dài và có thể hiển thị được
                if (errorBodyString.length() < 200) { // Giới hạn độ dài để tránh hiển thị log quá lớn
                    Log.w(TAG, "Error body is not JSON or parsing failed, returning raw error body: " + errorBodyString);
                    return errorBodyString;
                }
                Log.e(TAG, "JSONException while parsing error body: " + e.getMessage());
            }

        } catch (java.io.IOException e) {
            Log.e(TAG, "IOException while reading error body: " + e.getMessage());
            return "Error reading response from server (Code: " + response.code() + ")";
        }
        return "Unknown error occurred (Code: " + response.code() + ")";
    }
} 