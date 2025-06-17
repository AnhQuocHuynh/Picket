package com.example.locket.data.api;

import com.example.locket.data.model.AuthModels;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<AuthModels.AuthResponse> login(@Body AuthModels.LoginRequest request);

    @POST("auth/register")
    Call<AuthModels.AuthResponse> register(@Body AuthModels.RegisterRequest request);
} 