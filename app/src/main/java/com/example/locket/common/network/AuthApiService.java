package com.example.locket.common.network;

import com.example.locket.common.models.auth.LoginRequest;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.auth.RegisterRequest;
import com.example.locket.common.models.auth.AuthResponse;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.models.common.ApiResponse;
import com.example.locket.common.models.auth.VerifyEmailRequest;
import com.example.locket.common.models.auth.ResendVerificationRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApiService {

    // 🔐 USER REGISTRATION
    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // 🔑 USER LOGIN
    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // 👤 GET USER PROFILE (requires JWT token)
    @GET("auth/profile")
    Call<UserProfile> getUserProfile(@Header("Authorization") String bearerToken);

    // ✏️ UPDATE USER PROFILE (requires JWT token)
    @Headers({
            "Content-Type: application/json"
    })
    @PUT("auth/profile")
    Call<UserProfile> updateUserProfile(
            @Header("Authorization") String bearerToken,
            @Body UpdateProfileRequest request
    );

    // 🚪 LOGOUT (updates lastSeen)
    @POST("auth/logout")
    Call<ApiResponse> logout(@Header("Authorization") String bearerToken);

    // 📧 VERIFY EMAIL
    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/verify-email")
    Call<ApiResponse> verifyEmail(@Body VerifyEmailRequest request);

    // 🔄 RESEND VERIFICATION CODE
    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/send-verification-email")
    Call<ApiResponse> resendVerification(@Body ResendVerificationRequest request);

    // 🔄 RESEND VERIFICATION CODE (with token)
    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/send-verification-email")
    Call<ApiResponse> resendVerificationWithToken(@Header("Authorization") String bearerToken);

    // ❌ REMOVED ENDPOINTS (không có trong backend):
    // - auth/check-email (đã bỏ)
    // - auth/forgot-password (chưa implement)
    // - auth/refresh (backend không có refresh token)

    // Request classes
    public static class UpdateProfileRequest {
        private String username;
        private String profilePicture;

        public UpdateProfileRequest() {}

        public UpdateProfileRequest(String username, String profilePicture) {
            this.username = username;
            this.profilePicture = profilePicture;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }
    }
} 