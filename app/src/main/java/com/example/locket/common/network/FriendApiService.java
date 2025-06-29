package com.example.locket.common.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FriendApiService {

    // ❌ Backend không có friends endpoints - Comment out để tránh 404
    /*
    @Headers({
            "Content-Type: application/json"
    })
    @POST("friends/user")
    Call<ResponseBody> GET_FRIEND_RESPONSE_CALL(@Body RequestBody body);
    */

    // ✅ Placeholder - Backend chưa implement friends
    // Tạm thời disable tất cả friend-related API calls
    // Sẽ enable khi backend có sẵn endpoints
}
