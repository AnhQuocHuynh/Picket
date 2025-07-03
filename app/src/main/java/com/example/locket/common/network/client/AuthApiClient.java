package com.example.locket.common.network.client;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthApiClient {
    private static Retrofit authRetrofit = null;

    // 🔧 NETWORK CONFIGURATION - Backend thực tế với MongoDB Atlas

    // ✅ Option 1: Android Emulator (RECOMMENDED for real backend)
    private static final String AUTH_BASE_URL = "http://10.0.2.2:3000/api/";

    // ✅ Option 2: Physical Device (Thay YOUR_IP bằng IP thật của máy)
    // private static final String AUTH_BASE_URL = "http://192.168.1.102:3000/api/";

    // ✅ Option 3: Localhost cho desktop testing  
    // private static final String AUTH_BASE_URL = "http://localhost:3000/api/";

    // 📋 AVAILABLE ENDPOINTS trên backend thực tế:
    // ✅ POST /api/auth/register
    // ✅ POST /api/auth/login  
    // ✅ GET /api/auth/profile
    // ✅ POST /api/posts
    // ✅ GET /api/posts
    // ✅ POST /api/posts/:id/like
    // ✅ POST /api/posts/:id/comment
    // ❌ KHÔNG có: /api/auth/check-email (đã bỏ qua trong code)

    // 🚨 DEBUG: Full network logging enabled
    private static final boolean DEBUG_MODE = true;

    public static Retrofit getAuthClient() {
        if (authRetrofit == null) {
            Log.d("AuthApiClient", "🔗 Initializing AuthApiClient...");
            Log.d("AuthApiClient", "📡 Base URL: " + AUTH_BASE_URL);
            Log.d("AuthApiClient", "🏗️ Backend: Real MongoDB Atlas backend");
            Log.d("AuthApiClient", "🔧 Debug Mode: " + DEBUG_MODE);

            // Tạo logging interceptor để debug network calls
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                    Log.d("NetworkCall", message)
            );

            // Set log level tùy theo debug mode
            if (DEBUG_MODE) {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                Log.d("AuthApiClient", "🐛 Full network logging enabled");
            } else {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            }

            // Cấu hình OkHttpClient với timeout và logging
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor);

            // Add debug interceptor nếu cần
            if (DEBUG_MODE) {
                clientBuilder.addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    Log.d("AuthApiClient", "🚀 Making request to: " + request.url());
                    okhttp3.Response response = chain.proceed(request);
                    Log.d("AuthApiClient", "📥 Response status: " + response.code());
                    return response;
                });
            }

            OkHttpClient okHttpClient = clientBuilder.build();

            authRetrofit = new Retrofit.Builder()
                    .baseUrl(AUTH_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Log.d("AuthApiClient", "✅ AuthApiClient initialized successfully");
            Log.d("AuthApiClient", "📋 Available endpoints: login, register, profile, posts");
        }
        return authRetrofit;
    }

    // Helper method để get current base URL for debugging
    public static String getCurrentBaseUrl() {
        return AUTH_BASE_URL;
    }

    // Helper method để check backend type
    public static boolean isRealBackend() {
        return true; // Đây là backend thực tế với MongoDB
    }
} 