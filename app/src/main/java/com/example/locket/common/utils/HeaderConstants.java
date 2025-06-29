package com.example.locket.common.utils;

import java.util.HashMap;
import java.util.Map;

public class HeaderConstants {

    // Standard headers for backend API calls with JWT token
    public static Map<String, String> getAuthHeaders(String jwtToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("authorization", "Bearer " + jwtToken);
        headers.put("accept", "application/json");
        headers.put("user-agent", "Locket-Android/1.0");
        return headers;
    }

    // Headers for file upload to backend
    public static Map<String, String> getUploadHeaders(String jwtToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + jwtToken);
        headers.put("user-agent", "Locket-Android/1.0");
        return headers;
    }

    // Headers for multipart form data
    public static Map<String, String> getMultipartHeaders(String jwtToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + jwtToken);
        headers.put("content-type", "multipart/form-data");
        headers.put("user-agent", "Locket-Android/1.0");
        return headers;
    }

    // Legacy methods for backward compatibility với old API system

    // Method được gọi trong ApiCaller.java
    public static Map<String, String> getStartUploadHeaders(String idToken, int imageLength, boolean isVideo) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + idToken);
        headers.put("content-type", "application/json");
        headers.put("content-length", String.valueOf(imageLength));
        headers.put("user-agent", "Locket-Android/1.0");
        if (isVideo) {
            headers.put("x-media-type", "video");
        } else {
            headers.put("x-media-type", "image");
        }
        return headers;
    }

    // Method được gọi trong ApiCaller.java  
    public static Map<String, String> getUploadImageHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/octet-stream");
        headers.put("user-agent", "Locket-Android/1.0");
        return headers;
    }

    // Method được gọi trong ApiCaller.java
    public static Map<String, String> getPostHeaders(String idToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + idToken);
        headers.put("content-type", "application/json");
        headers.put("accept", "application/json");
        headers.put("user-agent", "Locket-Android/1.0");
        return headers;
    }
}
