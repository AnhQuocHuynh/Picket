package com.example.locket.common.network;

import com.example.locket.common.models.common.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserManagementApiService {

    // 🖼️ UPDATE USER AVATAR
    @Headers({
            "Content-Type: application/json"
    })
    @POST("user/avatar")
    Call<ApiResponse> updateAvatar(
            @Header("Authorization") String bearerToken,
            @Body UpdateAvatarRequest request
    );

    // 🗑️ DELETE USER AVATAR
    @DELETE("user/avatar")
    Call<ApiResponse> deleteAvatar(
            @Header("Authorization") String bearerToken
    );

    // Request class for avatar update
    public static class UpdateAvatarRequest {
        private String avatarUrl;

        public UpdateAvatarRequest() {}

        public UpdateAvatarRequest(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
} 