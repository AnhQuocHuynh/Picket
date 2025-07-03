package com.example.locket.common.utils;

import android.content.Context;
import android.util.Log;

import retrofit2.Response;

public class ApiErrorHandler {
    private static final String TAG = "ApiErrorHandler";

    public interface ErrorCallback {
        void onError(String message, int code);
        void onTokenExpired();
    }

    /**
     * Handle API response errors with standardized messages
     */
    public static void handleError(Response<?> response, ErrorCallback callback) {
        if (callback == null) return;

        int code = response.code();
        String message = getErrorMessage(code);

        Log.e(TAG, "API Error - Code: " + code + ", Message: " + message);

        // Handle token expiration
        if (code == 401) {
            callback.onTokenExpired();
        } else {
            callback.onError(message, code);
        }
    }

    /**
     * Handle network failures
     */
    public static void handleNetworkError(Throwable throwable, ErrorCallback callback) {
        if (callback == null) return;

        String message = "Network error: ";
        if (throwable.getMessage() != null) {
            message += throwable.getMessage();
        } else {
            message += "Please check your internet connection";
        }

        Log.e(TAG, "Network Error: " + message, throwable);
        callback.onError(message, -1);
    }

    /**
     * Get user-friendly error message based on HTTP status code
     */
    public static String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Invalid request. Please check your input.";
            case 401:
                return "Session expired. Please login again.";
            case 403:
                return "You don't have permission to access this resource.";
            case 404:
                return "The requested resource was not found.";
            case 409:
                return "This data already exists.";
            case 422:
                return "Validation failed. Please check your input.";
            case 429:
                return "Too many requests. Please try again later.";
            case 500:
                return "Server error. Please try again later.";
            case 502:
            case 503:
            case 504:
                return "Service temporarily unavailable. Please try again later.";
            default:
                return "An unexpected error occurred. (Code: " + statusCode + ")";
        }
    }

    /**
     * Clear authentication data when token expires
     */
    public static void clearAuthenticationData(Context context) {
        SharedPreferencesUser.clearAll(context);
        Log.d(TAG, "Authentication data cleared due to token expiration");
    }

    /**
     * Check if error is authentication-related
     */
    public static boolean isAuthenticationError(int statusCode) {
        return statusCode == 401 || statusCode == 403;
    }

    /**
     * Check if error is client-side (4xx)
     */
    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Check if error is server-side (5xx)
     */
    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
} 