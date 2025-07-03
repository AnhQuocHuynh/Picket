package com.example.locket.common.network.client;

import retrofit2.Retrofit;

public class LoginApiClient {

    // Sử dụng AuthApiClient để đảm bảo cùng cấu hình
    // Tất cả các method sẽ trả về cùng một Retrofit instance từ AuthApiClient

    // check email
    public static Retrofit getCheckEmailClient() {
        return AuthApiClient.getAuthClient();
    }

    // Retrofit cho việc đăng nhập
    public static Retrofit getLoginClient() {
        return AuthApiClient.getAuthClient();
    }

    // Retrofit cho việc refresh token
    public static Retrofit getRefreshTokenClient() {
        return AuthApiClient.getAuthClient();
    }
}

