package com.example.locket.common.utils;

import android.content.Context;
import android.util.Log;
import android.provider.MediaStore;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * CloudinaryManager - Quản lý upload ảnh lên Cloudinary
 * Implements theo flow: Android → Cloudinary → Backend → Database → Display
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static boolean isInitialized = false;

    // Cloudinary credentials - Thay đổi theo config thực tế
    private static final String CLOUD_NAME = "dygshicnm";
    private static final String API_KEY = "484476358813872";
    private static final String API_SECRET = "vl9qKnsvksj2EB8n09CAySWvZ7w";

    /**
     * Callback interface cho upload result
     */
    public interface CloudinaryUploadCallback {
        void onUploadStart(String requestId);
        void onUploadProgress(String requestId, int progress);
        void onUploadSuccess(String requestId, String publicUrl);
        void onUploadError(String requestId, String error);
        void onUploadReschedule(String requestId, String error);
    }

    /**
     * Initialize Cloudinary MediaManager
     */
    public static void initialize(Context context) {
        if (!isInitialized) {
            try {
                Map<String, Object> config = new HashMap<>();
                config.put("cloud_name", CLOUD_NAME);
                config.put("api_key", API_KEY);
                config.put("api_secret", API_SECRET);
                config.put("secure", true);

                MediaManager.init(context, config);
                isInitialized = true;
                Log.d(TAG, "✅ Cloudinary initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to initialize Cloudinary: " + e.getMessage());
            }
        }
    }

    /**
     * Upload image bytes to Cloudinary
     * @param imageBytes - byte array of image
     * @param fileName - name for the file
     * @param callback - callback interface
     */
    public static void uploadImage(byte[] imageBytes, String fileName, CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            if (callback != null) {
                callback.onUploadError("", "Cloudinary not initialized");
            }
            return;
        }

        if (imageBytes == null || imageBytes.length == 0) {
            if (callback != null) {
                callback.onUploadError("", "Image data is null or empty");
            }
            return;
        }

        try {
            // Upload options
            Map<String, Object> options = new HashMap<>();
            options.put("public_id", "locket_" + System.currentTimeMillis());
            options.put("folder", "locket_images");
            options.put("resource_type", "image");
            options.put("format", "jpg");
            options.put("quality", "auto");
            options.put("fetch_format", "auto");
            // 🔧 FIX: Giữ nguyên orientation và auto-rotate
            options.put("flags", "progressive");
            options.put("angle", "exif"); // Tự động xoay theo EXIF data

            Log.d(TAG, "🚀 Starting Cloudinary upload...");

            MediaManager.get()
                    .upload(imageBytes)
                    .options(options)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "📤 Upload started: " + requestId);
                            if (callback != null) {
                                callback.onUploadStart(requestId);
                            }
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            Log.d(TAG, "⏳ Upload progress: " + progress + "%");
                            if (callback != null) {
                                callback.onUploadProgress(requestId, progress);
                            }
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            try {
                                String publicUrl = (String) resultData.get("secure_url");
                                if (publicUrl == null) {
                                    publicUrl = (String) resultData.get("url");
                                }

                                Log.d(TAG, "✅ Upload successful!");
                                Log.d(TAG, "🔗 Public URL: " + publicUrl);

                                if (callback != null) {
                                    callback.onUploadSuccess(requestId, publicUrl);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parsing upload result: " + e.getMessage());
                                if (callback != null) {
                                    callback.onUploadError(requestId, "Error parsing upload result");
                                }
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "❌ Upload error: " + error.getDescription());
                            if (callback != null) {
                                callback.onUploadError(requestId, error.getDescription());
                            }
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "⏳ Upload rescheduled: " + error.getDescription());
                            if (callback != null) {
                                callback.onUploadReschedule(requestId, error.getDescription());
                            }
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            Log.e(TAG, "❌ Upload exception: " + e.getMessage());
            if (callback != null) {
                callback.onUploadError("", e.getMessage());
            }
        }
    }

    /**
     * Upload video bytes to Cloudinary
     * @param videoBytes - byte array of video
     * @param fileName - name for the file
     * @param callback - callback interface
     */
    public static void uploadVideo(byte[] videoBytes, String fileName, CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            if (callback != null) {
                callback.onUploadError("", "Cloudinary not initialized");
            }
            return;
        }
        if (videoBytes == null || videoBytes.length == 0) {
            if (callback != null) {
                callback.onUploadError("", "Video data is null or empty");
            }
            return;
        }
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("public_id", "locket_video_" + System.currentTimeMillis());
            options.put("folder", "locket_videos");
            options.put("resource_type", "video");
            options.put("format", "mp4");
            Log.d(TAG, "🚀 Starting Cloudinary video upload...");
            MediaManager.get()
                    .upload(videoBytes)
                    .options(options)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            if (callback != null) callback.onUploadStart(requestId);
                        }
                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            if (callback != null) callback.onUploadProgress(requestId, progress);
                        }
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String publicUrl = (String) resultData.get("secure_url");
                            if (publicUrl == null) publicUrl = (String) resultData.get("url");
                            if (callback != null) callback.onUploadSuccess(requestId, publicUrl);
                        }
                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            if (callback != null) callback.onUploadError(requestId, error.getDescription());
                        }
                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            if (callback != null) callback.onUploadReschedule(requestId, error.getDescription());
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            if (callback != null) callback.onUploadError("", e.getMessage());
        }
    }

    /**
     * Check if Cloudinary is initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Get Cloudinary configuration info
     */
    public static String getCloudName() {
        return CLOUD_NAME;
    }
} 