package com.example.locket.common.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MomentApiService {

    // ❌ Backend không có moments endpoints - Comment out để tránh 404
    /*
    @Headers({
            "Content-Type: application/json"
    })
    @POST("moments/latest")
    Call<ResponseBody> GET_MOMENT_RESPONSE_CALL(@Body RequestBody body);
    */

    // ✅ Placeholder - Backend chưa implement moments
    // Tạm thời disable tất cả moment-related API calls
    // Sẽ enable khi backend có sẵn endpoints

}

