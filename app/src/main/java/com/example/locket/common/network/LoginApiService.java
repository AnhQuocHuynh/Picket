package com.example.locket.common.network;

import com.example.locket.common.models.auth.LoginRequest;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.user.UserProfile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * 🔑 LOGIN API SERVICE
 *
 * ✅ COMPATIBLE WITH REAL BACKEND
 * ❌ Removed non-existent endpoints
 *
 * Base URL: http://10.0.2.2:3000/api/
 */
public interface LoginApiService {

    // ✅ USER LOGIN - Backend endpoint exists
    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ✅ GET USER PROFILE - Backend endpoint exists  
    @GET("auth/profile")
    Call<UserProfile> getUserProfile(@Header("Authorization") String bearerToken);

    // ❌ REMOVED ENDPOINTS (không tồn tại trong backend):
    // - POST auth/check-email (backend không có)
    // - POST auth/refresh (backend không support refresh token)
    // - POST auth/profile (chỉ có GET, không có POST)

    /**
     * 📝 USAGE EXAMPLES:
     *
     * // Login
     * LoginRequest request = new LoginRequest("user@example.com", "password123");
     * Call<LoginResponse> call = loginService.login(request);
     *
     * // Get Profile
     * String token = "Bearer " + SharedPreferencesUser.getJWTToken(context);
     * Call<UserProfile> profileCall = loginService.getUserProfile(token);
     */
} 