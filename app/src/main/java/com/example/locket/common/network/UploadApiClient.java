package com.example.locket.common.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadApiClient {
    private static Retrofit uploadRetrofit = null;
    private static Retrofit apiRetrofit = null;
    
    // Backend API URLs - cập nhật theo backend của bạn
    private static final String BASE_API_URL = "http://localhost:3000/api/";
    private static final String BASE_UPLOAD_URL = "http://localhost:3000/api/upload/";

    // Upload client for media files (images/videos)
    public static Retrofit getUploadClient() {
        if (uploadRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            uploadRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_UPLOAD_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return uploadRetrofit;
    }

    // API client for general backend calls
    public static Retrofit getApiClient() {
        if (apiRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            apiRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_API_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return apiRetrofit;
    }

    // Legacy methods for backward compatibility - redirect to new methods
    @Deprecated
    public static Retrofit getUploadImageRetrofit() {
        return getUploadClient();
    }

    @Deprecated
    public static Retrofit getUploadVideoRetrofit() {
        return getUploadClient();
    }

    @Deprecated
    public static Retrofit getPostRetrofit() {
        return getApiClient();
    }
}
