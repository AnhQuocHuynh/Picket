package com.example.locket.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.locket.common.models.auth.LoginRequest;
import com.example.locket.common.models.auth.LoginRespone;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.user.AccountInfo;
import com.example.locket.common.models.user.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SharedPreferencesUser {
    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "user_prefs";
    private static final String LOGIN_REQUEST = "login_request";
    private static final String LOGIN_RESPONSE = "login_response";
    private static final String LOGIN_RESPONSE_OLD = "login_response_old"; // For LoginRespone
    private static final String USER_PROFILE = "user_profile";
    private static final String JWT_TOKEN = "jwt_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String USER_FRIENDS = "user_friends";
    private static final String ACCOUNT_INFO = "account_info";
    private static final String PENDING_FRIEND_TOKEN = "pending_friend_token";

    public static void saveLoginRequest(Context context, LoginRequest loginRequest) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loginRequest);
        editor.putString(LOGIN_REQUEST, json);
        editor.apply();
    }

    public static LoginRequest getLoginRequest(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(LOGIN_REQUEST, null);
        Gson gson = new Gson();
        return gson.fromJson(json, LoginRequest.class);
    }

    public static void saveLoginResponse(Context context, LoginResponse loginResponse) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loginResponse);
        editor.putString(LOGIN_RESPONSE, json);
        editor.apply();
    }

    public static LoginResponse getLoginResponse(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(LOGIN_RESPONSE, null);
        Gson gson = new Gson();
        return gson.fromJson(json, LoginResponse.class);
    }

    // Support cho LoginRespone (backward compatibility)
    public static void saveLoginResponse(Context context, LoginRespone loginRespone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loginRespone);
        editor.putString(LOGIN_RESPONSE_OLD, json);
        editor.apply();
    }

    public static LoginRespone getLoginRespone(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(LOGIN_RESPONSE_OLD, null);
        Gson gson = new Gson();
        return gson.fromJson(json, LoginRespone.class);
    }

    // AccountInfo methods with correct type
    public static void saveAccountInfo(Context context, AccountInfo accountInfo) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(accountInfo);
        editor.putString(ACCOUNT_INFO, json);
        editor.apply();
    }

    public static AccountInfo getAccountInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(ACCOUNT_INFO, null);
        Gson gson = new Gson();
        return gson.fromJson(json, AccountInfo.class);
    }

    public static void saveUserProfile(Context context, UserProfile userProfile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userProfile);
        editor.putString(USER_PROFILE, json);
        editor.apply();
    }

    public static UserProfile getUserProfile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(USER_PROFILE, null);
        Gson gson = new Gson();
        return gson.fromJson(json, UserProfile.class);
    }

    // JWT Token methods
    public static void saveJWTToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(JWT_TOKEN, token);
        editor.apply();
    }

    public static String getJWTToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(JWT_TOKEN, null);
    }

    public static void saveRefreshToken(Context context, String refreshToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public static String getRefreshToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(REFRESH_TOKEN, null);
    }

    public static void saveUserFriends(Context context, List<String> user_friends) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user_friends);
        editor.putString(USER_FRIENDS, json);
        editor.apply();
    }

    public static List<String> getUserFriends(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(USER_FRIENDS, null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Clear methods
    public static void clearLoginData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LOGIN_RESPONSE);
        editor.remove(LOGIN_RESPONSE_OLD);
        editor.remove(LOGIN_REQUEST);
        editor.remove(JWT_TOKEN);
        editor.remove(REFRESH_TOKEN);
        editor.apply();
    }

    public static void clearAll(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Utility method to check if user is logged in
    public static boolean isLoggedIn(Context context) {
        String token = getJWTToken(context);
        return token != null && !token.isEmpty();
    }

    // Pending friend token methods for deep linking
    public static void savePendingFriendToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PENDING_FRIEND_TOKEN, token);
        editor.apply();
    }

    public static String getPendingFriendToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PENDING_FRIEND_TOKEN, null);
    }

    public static void clearPendingFriendToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PENDING_FRIEND_TOKEN);
        editor.apply();
    }
}

