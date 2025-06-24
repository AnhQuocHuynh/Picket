package com.example.locket.data.repository;

import android.util.Log;
import com.example.locket.data.api.AuthApi;
import com.example.locket.data.model.AuthModels;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final AuthApi authApi;

    @Inject
    public AuthRepository(AuthApi authApi) {
        this.authApi = authApi;
    }

    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting login for email: " + email);
        AuthModels.LoginRequest request = new AuthModels.LoginRequest(email, password);
        Call<AuthModels.AuthResponse> call = authApi.login(request);
        
        call.enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                Log.d(TAG, "Login response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.AuthResponse authResponse = response.body();
                    if (authResponse.getData() != null && authResponse.getData().getUser() != null) {
                        Log.d(TAG, "Login successful for user: " + authResponse.getData().getUser().getUsername());
                    } else {
                        Log.d(TAG, "Login successful but user data is null");
                    }
                    callback.onSuccess(authResponse);
                } else {
                    String errorMsg = "Login failed with code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += ", Unable to read error body";
                        }
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                String errorMsg = "Network error during login: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void register(String username, String email, String password, String fullName, AuthCallback callback) {
        Log.d(TAG, "Attempting registration for username: " + username + ", email: " + email);
        AuthModels.RegisterRequest request = new AuthModels.RegisterRequest(username, email, password, fullName);
        Call<AuthModels.AuthResponse> call = authApi.register(request);
        
        call.enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                Log.d(TAG, "Register response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.AuthResponse authResponse = response.body();
                    if (authResponse.getData() != null && authResponse.getData().getUser() != null) {
                        Log.d(TAG, "Registration successful for user: " + authResponse.getData().getUser().getUsername());
                    } else {
                        Log.d(TAG, "Registration successful but user data is null");
                    }
                    callback.onSuccess(authResponse);
                } else {
                    String errorMsg = "Registration failed with code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ", Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += ", Unable to read error body";
                        }
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                String errorMsg = "Network error during registration: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public interface AuthCallback {
        void onSuccess(AuthModels.AuthResponse response);
        void onError(String error);
    }
} 