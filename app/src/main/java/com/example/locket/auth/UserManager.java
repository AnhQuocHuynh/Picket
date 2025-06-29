package com.example.locket.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private static UserManager instance;
    private SharedPreferences prefs;

    private UserManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setUserType(UserType userType) {
        prefs.edit().putString(KEY_USER_TYPE, userType.getValue()).apply();
    }

    public UserType getUserType() {
        String userTypeStr = prefs.getString(KEY_USER_TYPE, UserType.FREE.getValue());
        return UserType.fromString(userTypeStr);
    }

    public boolean isProUser() {
        return getUserType() == UserType.PRO;
    }

    public void setUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public void setUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public void clearUserData() {
        prefs.edit().clear().apply();
    }
}