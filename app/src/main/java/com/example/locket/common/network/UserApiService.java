package com.example.locket.common.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserApiService {

    // ❌ Backend không có user endpoints - Comment out tất cả để tránh 404
    /*
    @Headers({
            "Content-Type: application/json"
    })
    @POST("user/change-profile")
    Call<ResponseBody> USER_CHANGE_PROFILE_RESPONSE_CALL(@Body RequestBody body);

    @Headers({
            "Content-Type: application/json"
    })
    @POST("user/validate-username")
    Call<ResponseBody> USER_VALIDATE_USERNAME_RESPONSE_CALL(@Body RequestBody body);

    @Headers({
            "Content-Type: application/json"
    })
    @POST("user/discoverability")
    Call<ResponseBody> USER_DISCOVERABILITY_RESPONSE_CALL(@Body RequestBody body);
    */

    // ✅ Placeholder - Backend chưa implement user management
    // Tạm thời disable tất cả user-related API calls
    // Sẽ enable khi backend có sẵn endpoints

    // Change profile info
    @Headers({
            "Content-Type: application/json",
            "User-Agent: Locket-Android/1.0"
    })
    @POST("user/change-profile")
    Call<ResponseBody> CHANGE_NAME_RESPONSE_CALL(
            @Header("Authorization") String token,
            @Body RequestBody body
    );

    // Check username availability
    @Headers({
            "Content-Type: application/json",
            "User-Agent: Locket-Android/1.0"
    })
    @POST("user/validate-username")
    Call<ResponseBody> CHECK_USERNAME_RESPONSE_CALL(
            @Header("Authorization") String token,
            @Body RequestBody body
    );

    // Update username discoverability
    @Headers({
            "Content-Type: application/json",
            "User-Agent: Locket-Android/1.0"
    })
    @POST("user/discoverability")
    Call<ResponseBody> username_discoverability_disabled(
            @Header("Authorization") String token,
            @Body RequestBody body
    );

}
