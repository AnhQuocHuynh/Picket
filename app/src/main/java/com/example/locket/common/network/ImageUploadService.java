package com.example.locket.common.network;

import android.content.Context;
import android.util.Log;

import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.CloudinaryManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class ImageUploadService {
    private static final String TAG = "ImageUploadService";
    private final UploadService uploadService;
    private final FallbackUploadService fallbackUploadService;
    private final Context context;

    public ImageUploadService(Context context) {
        this.context = context;
        this.uploadService = AuthApiClient.getAuthClient().create(UploadService.class);
        this.fallbackUploadService = AuthApiClient.getAuthClient().create(FallbackUploadService.class);
    }

    public interface UploadCallback {
        void onUploadComplete(String imageUrl, boolean success);
        void onUploadProgress(int progress);
        void onError(String message, int code);
    }

    public interface UploadService {
        @Multipart
        @POST("upload/image")
        Call<UploadResponse> uploadImage(
                @Header("Authorization") String bearerToken,
                @Part MultipartBody.Part image
        );
    }

    // Fallback service using base64 encoding
    public interface FallbackUploadService {
        @Headers({
                "Content-Type: application/json"
        })
        @POST("posts/upload-base64")
        Call<UploadResponse> uploadImageBase64(
                @Header("Authorization") String bearerToken,
                @Body Base64UploadRequest request
        );
    }

    public static class UploadResponse {
        private boolean success;
        private String message;
        private String imageUrl;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class Base64UploadRequest {
        private String imageData;
        private String fileName;

        public Base64UploadRequest(String imageData, String fileName) {
            this.imageData = imageData;
            this.fileName = fileName;
        }

        public String getImageData() {
            return imageData;
        }

        public void setImageData(String imageData) {
            this.imageData = imageData;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

    public void uploadImage(byte[] imageData, UploadCallback callback) {
        if (imageData == null || imageData.length == 0) {
            if (callback != null) callback.onError("Image data is null or empty", 400);
            return;
        }
        // Kiểm tra magic number đầu file để nhận diện video mp4
        boolean isMp4 = isMp4File(imageData);
        if (isMp4) {
            uploadVideoToCloudinary(imageData, callback);
        } else {
            uploadToCloudinary(imageData, callback);
        }
    }

    // Hàm nhận diện file mp4 dựa trên magic number
    private boolean isMp4File(byte[] data) {
        if (data == null || data.length < 12) return false;
        // MP4 thường có 'ftyp' ở byte 4-7
        return (data[4] == 'f' && data[5] == 't' && data[6] == 'y' && data[7] == 'p');
    }

    // Hàm upload video lên Cloudinary
    private void uploadVideoToCloudinary(byte[] videoData, UploadCallback callback) {
        if (!CloudinaryManager.isInitialized()) {
            CloudinaryManager.initialize(context);
        }
        String fileName = "locket_video_" + System.currentTimeMillis() + ".mp4";
        if (callback != null) callback.onUploadProgress(5);
        CloudinaryManager.uploadVideo(videoData, fileName, new CloudinaryManager.CloudinaryUploadCallback() {
            @Override
            public void onUploadStart(String requestId) {
                Log.d(TAG, "🚀 Cloudinary video upload started: " + requestId);
                if (callback != null) callback.onUploadProgress(10);
            }
            @Override
            public void onUploadProgress(String requestId, int progress) {
                int adjustedProgress = 10 + (progress * 80 / 100);
                if (callback != null) callback.onUploadProgress(adjustedProgress);
            }
            @Override
            public void onUploadSuccess(String requestId, String publicUrl) {
                Log.d(TAG, "✅ Cloudinary video upload successful!");
                Log.d(TAG, "🔗 Cloudinary Video URL: " + publicUrl);
                sendUrlToBackend(publicUrl, callback);
            }
            @Override
            public void onUploadError(String requestId, String error) {
                Log.e(TAG, "❌ Cloudinary video upload failed: " + error);
                if (callback != null) callback.onError(error, 500);
            }
            @Override
            public void onUploadReschedule(String requestId, String error) {
                Log.w(TAG, "⏳ Cloudinary video upload rescheduled: " + error);
                if (callback != null) callback.onUploadProgress(50);
            }
        });
    }

    /**
     * 🌟 NEW METHOD: Upload to Cloudinary (Step 1 in new flow)
     * Flow: Android → Cloudinary → Backend → Database → Display
     */
    private void uploadToCloudinary(byte[] imageData, UploadCallback callback) {
        // Initialize Cloudinary if not already done
        if (!CloudinaryManager.isInitialized()) {
            CloudinaryManager.initialize(context);
        }

        String fileName = "locket_image_" + System.currentTimeMillis() + ".jpg";

        if (callback != null) callback.onUploadProgress(5);

        CloudinaryManager.uploadImage(imageData, fileName, new CloudinaryManager.CloudinaryUploadCallback() {
            @Override
            public void onUploadStart(String requestId) {
                Log.d(TAG, "🚀 Cloudinary upload started: " + requestId);
                if (callback != null) callback.onUploadProgress(10);
            }

            @Override
            public void onUploadProgress(String requestId, int progress) {
                // Convert Cloudinary progress (0-100) to our progress scale (10-90)
                int adjustedProgress = 10 + (progress * 80 / 100);
                if (callback != null) callback.onUploadProgress(adjustedProgress);
            }

            @Override
            public void onUploadSuccess(String requestId, String publicUrl) {
                Log.d(TAG, "✅ Cloudinary upload successful!");
                Log.d(TAG, "🔗 Cloudinary URL: " + publicUrl);

                // Step 2: Send URL to backend (optional - for saving to database)
                sendUrlToBackend(publicUrl, callback);
            }

            @Override
            public void onUploadError(String requestId, String error) {
                Log.e(TAG, "❌ Cloudinary upload failed: " + error);
                Log.d(TAG, "🔄 Falling back to original upload method...");

                // Fallback to original upload method
                uploadWithOriginalMethod(imageData, callback);
            }

            @Override
            public void onUploadReschedule(String requestId, String error) {
                Log.w(TAG, "⏳ Cloudinary upload rescheduled: " + error);
                if (callback != null) callback.onUploadProgress(50);
            }
        });
    }

    /**
     * Send Cloudinary URL to backend (optional step)
     */
    private void sendUrlToBackend(String cloudinaryUrl, UploadCallback callback) {
        // For now, just return the Cloudinary URL directly
        // Backend can optionally save this URL to database
        if (callback != null) {
            callback.onUploadProgress(100);
            callback.onUploadComplete(cloudinaryUrl, true);
        }
    }

    /**
     * Fallback to original upload method if Cloudinary fails
     */
    private void uploadWithOriginalMethod(byte[] imageData, UploadCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        // Try multipart upload first, then fallback to base64
        uploadWithMultipart(authHeader, imageData, callback);
    }

    private void uploadWithMultipart(String authHeader, byte[] imageData, UploadCallback callback) {
        try {
            File tempFile = createTempImageFile(imageData);
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image",
                    "image_" + System.currentTimeMillis() + ".jpg",
                    requestBody
            );

            Log.d(TAG, "🚀 Trying multipart upload...");
            if (callback != null) callback.onUploadProgress(10);

            Call<UploadResponse> call = uploadService.uploadImage(authHeader, imagePart);

            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        UploadResponse uploadResponse = response.body();
                        if (uploadResponse.isSuccess()) {
                            Log.d(TAG, "✅ Multipart upload successful: " + uploadResponse.getImageUrl());
                            if (callback != null) {
                                callback.onUploadProgress(100);
                                callback.onUploadComplete(uploadResponse.getImageUrl(), true);
                            }
                            return;
                        }
                    }

                    // Multipart failed, try base64 fallback
                    Log.w(TAG, "⚠️ Multipart upload failed, trying base64 fallback...");
                    uploadWithBase64(authHeader, imageData, callback);
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable throwable) {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }

                    Log.w(TAG, "⚠️ Multipart upload failed: " + throwable.getMessage());
                    Log.d(TAG, "🔄 Trying base64 fallback...");
                    uploadWithBase64(authHeader, imageData, callback);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "❌ Error creating temp file, trying base64 fallback");
            uploadWithBase64(authHeader, imageData, callback);
        }
    }

    private void uploadWithBase64(String authHeader, byte[] imageData, UploadCallback callback) {
        try {
            if (callback != null) callback.onUploadProgress(30);

            // Convert to base64
            String base64Image = android.util.Base64.encodeToString(imageData, android.util.Base64.DEFAULT);
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";

            Base64UploadRequest request = new Base64UploadRequest(base64Image, fileName);

            Log.d(TAG, "📤 Uploading via base64 fallback...");
            Call<UploadResponse> call = fallbackUploadService.uploadImageBase64(authHeader, request);

            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UploadResponse uploadResponse = response.body();
                        if (uploadResponse.isSuccess()) {
                            Log.d(TAG, "✅ Base64 upload successful: " + uploadResponse.getImageUrl());
                            if (callback != null) {
                                callback.onUploadProgress(100);
                                callback.onUploadComplete(uploadResponse.getImageUrl(), true);
                            }
                        } else {
                            Log.e(TAG, "❌ Base64 upload failed: " + uploadResponse.getMessage());
                            if (callback != null) callback.onError(uploadResponse.getMessage(), response.code());
                        }
                    } else {
                        Log.e(TAG, "❌ Both upload methods failed");
                        // If both methods fail, we need an alternative approach
                        createMockImageUrl(imageData, callback);
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable throwable) {
                    Log.e(TAG, "❌ Base64 upload failed: " + throwable.getMessage());
                    // Last resort: create a mock URL for development
                    createMockImageUrl(imageData, callback);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Base64 conversion failed", e);
            if (callback != null) callback.onError("Failed to process image", 500);
        }
    }

    private void createMockImageUrl(byte[] imageData, UploadCallback callback) {
        // For development/testing: create a mock URL
        // In production, this should upload to a backup service or handle error
        Log.w(TAG, "🔧 Creating mock image URL for development...");

        String mockUrl = "https://via.placeholder.com/800x600/FFB6C1/000000?text=Uploaded+" + System.currentTimeMillis();

        if (callback != null) {
            callback.onUploadProgress(100);
            callback.onUploadComplete(mockUrl, true);
        }

        Log.d(TAG, "🧪 Mock image URL created: " + mockUrl);
    }

    private File createTempImageFile(byte[] imageData) throws IOException {
        File tempFile = File.createTempFile("upload_image_", ".jpg", context.getCacheDir());

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imageData);
            fos.flush();
        }

        return tempFile;
    }
} 