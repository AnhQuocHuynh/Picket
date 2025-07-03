package com.example.locket.common.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.locket.feed.widget.provider.LocketWidgetProvider;
import com.example.locket.feed.widget.service.LocketWidgetService;

/**
 * Helper class to manage widget cache and updates when user changes
 */
public class WidgetUpdateHelper {
    private static final String TAG = "WidgetUpdateHelper";
    private static final String WIDGET_PREFS_NAME = "widget_prefs";
    private static final String USER_PREFS_NAME = "user_prefs";
    private static final String CURRENT_USER_ID_KEY = "current_user_id";
    private static final String LAST_POST_ID = "last_post_id";
    private static final String LAST_SYNC_TIME = "last_sync_time";

    // Broadcast action for user changed
    public static final String ACTION_USER_CHANGED = "com.example.locket.ACTION_USER_CHANGED";

    /**
     * Trigger widget update when user logs in successfully
     */
    public static void onUserLoginSuccess(Context context) {
        Log.d(TAG, "User login success - checking if user changed and updating widget");

        String newUserId = getCurrentUserId(context);
        String lastUserId = getLastKnownUserId(context);

        Log.d(TAG, "New user ID: " + newUserId + ", Last user ID: " + lastUserId);

        // Check if user has changed
        boolean userChanged = (newUserId != null && !newUserId.equals(lastUserId)) ||
                (lastUserId != null && !lastUserId.equals(newUserId));

        if (userChanged) {
            Log.d(TAG, "User changed detected! Clearing widget cache and triggering update");
            clearWidgetCache(context);
            saveCurrentUserId(context, newUserId);
        } else {
            Log.d(TAG, "Same user logged in, just triggering widget update");
        }

        // Always trigger widget update on successful login
        triggerWidgetUpdate(context);

        // Send broadcast to notify widget provider
        sendUserChangedBroadcast(context);
    }

    /**
     * Clear widget cache when user logs out
     */
    public static void onUserLogout(Context context) {
        Log.d(TAG, "User logout - clearing widget cache");
        clearWidgetCache(context);
        clearCurrentUserId(context);

        // Trigger widget update to show logged out state
        triggerWidgetUpdate(context);
    }

    /**
     * Clear all widget-related cache data
     */
    public static void clearWidgetCache(Context context) {
        Log.d(TAG, "Clearing widget cache");
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(LAST_POST_ID);
        editor.remove(LAST_SYNC_TIME);
        editor.apply();
        Log.d(TAG, "Widget cache cleared successfully");
    }

    /**
     * Trigger immediate widget update
     */
    public static void triggerWidgetUpdate(Context context) {
        Log.d(TAG, "Triggering immediate widget update");

        // Check if any widgets are present
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, LocketWidgetProvider.class));

        if (appWidgetIds.length > 0) {
            Log.d(TAG, "Found " + appWidgetIds.length + " widget instances, triggering update");

            // Start service to fetch new data
            Intent serviceIntent = new Intent(context, LocketWidgetService.class);
            serviceIntent.setAction("ACTION_UPDATE_WIDGET");
            context.startService(serviceIntent);

            // Also send broadcast to widget provider
            Intent widgetIntent = new Intent(context, LocketWidgetProvider.class);
            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(widgetIntent);
        } else {
            Log.d(TAG, "No widget instances found, skipping update");
        }
    }

    /**
     * Send broadcast to notify about user change
     */
    public static void sendUserChangedBroadcast(Context context) {
        Log.d(TAG, "Sending user changed broadcast");
        Intent intent = new Intent(ACTION_USER_CHANGED);
        intent.setPackage(context.getPackageName()); // Only send to our app
        context.sendBroadcast(intent);
    }

    /**
     * Get current user ID from saved login data
     */
    private static String getCurrentUserId(Context context) {
        // Try to get from UserProfile first
        com.example.locket.common.models.user.UserProfile userProfile = SharedPreferencesUser.getUserProfile(context);
        if (userProfile != null && userProfile.getUser() != null && userProfile.getUser().getId() != null) {
            return userProfile.getUser().getId();
        }

        // Fallback to LoginResponse
        com.example.locket.common.models.auth.LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(context);
        if (loginResponse != null && loginResponse.getUser() != null && loginResponse.getUser().getId() != null) {
            return loginResponse.getUser().getId();
        }

        return null;
    }

    /**
     * Get last known user ID
     */
    private static String getLastKnownUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(CURRENT_USER_ID_KEY, null);
    }

    /**
     * Save current user ID
     */
    private static void saveCurrentUserId(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (userId != null) {
            editor.putString(CURRENT_USER_ID_KEY, userId);
        } else {
            editor.remove(CURRENT_USER_ID_KEY);
        }
        editor.apply();
        Log.d(TAG, "Saved current user ID: " + userId);
    }

    /**
     * Clear current user ID
     */
    private static void clearCurrentUserId(Context context) {
        saveCurrentUserId(context, null);
    }

    /**
     * Check if user is currently logged in
     */
    public static boolean isUserLoggedIn(Context context) {
        return AuthManager.isLoggedIn(context);
    }

    /**
     * Format relative time (e.g., "2 hours ago")
     */
    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) {
            return "Just now";
        } else if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + "m ago";
        } else if (diff < 24 * 60 * 60 * 1000) {
            long hours = diff / (60 * 60 * 1000);
            return hours + "h ago";
        } else {
            long days = diff / (24 * 60 * 60 * 1000);
            return days + "d ago";
        }
    }

    /**
     * Validate content and sanitize if needed
     */
    public static String sanitizeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        // Limit content length for widget display
        if (content.length() > 100) {
            return content.substring(0, 97) + "...";
        }

        return content.trim();
    }

    /**
     * Check if cache is still valid (within 24 hours)
     */
    public static boolean isCacheValid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        long lastSyncTime = prefs.getLong(LAST_SYNC_TIME, 0);
        long now = System.currentTimeMillis();
        long maxCacheAge = 24 * 60 * 60 * 1000; // 24 hours

        return (now - lastSyncTime) < maxCacheAge;
    }
} 