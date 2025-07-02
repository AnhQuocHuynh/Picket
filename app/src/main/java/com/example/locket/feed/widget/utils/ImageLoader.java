package com.example.locket.feed.widget.utils;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.locket.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;

    public interface ImageLoadCallback {
        void onImageLoaded(Bitmap bitmap);
        void onImageFailed(String error);
    }

    public void loadImageIntoWidget(Context context, String imageUrl, int widgetId, int viewId, AppWidgetManager appWidgetManager) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.w(TAG, "Image URL is null or empty for widget " + widgetId);
            setPlaceholderImage(context, widgetId, viewId, appWidgetManager);
            return;
        }

        // Check network connectivity first
        if (!isNetworkAvailable(context)) {
            Log.w(TAG, "No network connectivity available for loading image");
            setPlaceholderImage(context, widgetId, viewId, appWidgetManager);
            return;
        }

        Log.d(TAG, "Loading image for widget " + widgetId + ", view " + viewId + ": " + imageUrl);
        
        new Thread(() -> {
            try {
                Bitmap bitmap = loadImageFromUrl(imageUrl);
                if (bitmap != null) {
                    // Get current widget views and update only the specific image view
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);
                    views.setImageViewBitmap(viewId, bitmap);
                    
                    // Partial update - only update this specific view
                    appWidgetManager.partiallyUpdateAppWidget(widgetId, views);
                    Log.d(TAG, "Image loaded successfully into widget " + widgetId + ", view " + viewId);
                } else {
                    Log.e(TAG, "Failed to load image from URL: " + imageUrl);
                    setPlaceholderImage(context, widgetId, viewId, appWidgetManager);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image into widget: " + e.getMessage(), e);
                setPlaceholderImage(context, widgetId, viewId, appWidgetManager);
            }
        }).start();
    }

    private void setPlaceholderImage(Context context, int widgetId, int viewId, AppWidgetManager appWidgetManager) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);
            
            // Set appropriate placeholder based on view ID
            if (viewId == R.id.friend_avatar) {
                views.setImageViewResource(viewId, R.drawable.default_avatar);
                Log.d(TAG, "Set default avatar placeholder for widget " + widgetId);
            } else {
                views.setImageViewResource(viewId, R.drawable.widget_background);
                Log.d(TAG, "Set default background placeholder for widget " + widgetId);
            }
            
            appWidgetManager.partiallyUpdateAppWidget(widgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Error setting placeholder image for widget " + widgetId + ": " + e.getMessage(), e);
        }
    }

    public void loadImageAsync(String imageUrl, ImageLoadCallback callback) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            if (callback != null) callback.onImageFailed("Image URL is null or empty");
            return;
        }

        new Thread(() -> {
            try {
                Bitmap bitmap = loadImageFromUrl(imageUrl);
                if (bitmap != null) {
                    if (callback != null) callback.onImageLoaded(bitmap);
                } else {
                    if (callback != null) callback.onImageFailed("Failed to load image");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage(), e);
                if (callback != null) callback.onImageFailed(e.getMessage());
            }
        }).start();
    }

    private Bitmap loadImageFromUrl(String imageUrl) {
        HttpURLConnection connection = null;
        InputStream input = null;
        
        try {
            // Validate URL first
            if (!isValidImageUrl(imageUrl)) {
                Log.e(TAG, "Invalid image URL: " + imageUrl);
                return null;
            }
            
            Log.d(TAG, "Downloading image from: " + imageUrl);
            
            // Try with retry mechanism for network issues
            Bitmap bitmap = loadImageWithRetry(imageUrl, 2); // 2 retries
            
            if (bitmap != null) {
                // Resize bitmap for widget to optimize memory
                bitmap = resizeBitmapForWidget(bitmap);
                Log.d(TAG, "Image loaded and resized successfully. Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            }
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from URL: " + e.getMessage(), e);
            return null;
        }
    }

    private Bitmap loadImageWithRetry(String imageUrl, int maxRetries) {
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            HttpURLConnection connection = null;
            InputStream input = null;
            
            try {
                if (attempt > 0) {
                    Log.d(TAG, "Retry attempt " + attempt + " for image: " + imageUrl);
                    // Wait before retry
                    Thread.sleep(1000 * attempt); // Progressive delay
                }
                
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setDoInput(true);
                connection.setRequestProperty("User-Agent", "Locket-Widget/1.0");
                
                // Add headers to improve compatibility
                connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                connection.setRequestProperty("Cache-Control", "no-cache");
                
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Image download response code: " + responseCode + " (attempt " + (attempt + 1) + ")");
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    
                    if (bitmap != null) {
                        Log.d(TAG, "Image loaded successfully on attempt " + (attempt + 1));
                        return bitmap;
                    } else {
                        Log.w(TAG, "Decoded bitmap is null, may be invalid image format");
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Log.e(TAG, "Image not found (404): " + imageUrl);
                    break; // Don't retry for 404
                } else if (responseCode >= 400 && responseCode < 500) {
                    Log.e(TAG, "Client error " + responseCode + ", not retrying");
                    break; // Don't retry for client errors
                } else {
                    Log.w(TAG, "Server error " + responseCode + ", will retry if attempts remain");
                }
            } catch (java.net.UnknownHostException e) {
                Log.e(TAG, "DNS resolution failed for: " + imageUrl + " (attempt " + (attempt + 1) + ")");
                if (attempt == maxRetries) {
                    Log.e(TAG, "All DNS resolution attempts failed for: " + imageUrl);
                }
            } catch (java.net.ConnectException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage() + " (attempt " + (attempt + 1) + ")");
                if (attempt == maxRetries) {
                    Log.e(TAG, "All connection attempts failed for: " + imageUrl);
                }
            } catch (java.net.SocketTimeoutException e) {
                Log.e(TAG, "Timeout loading image: " + e.getMessage() + " (attempt " + (attempt + 1) + ")");
                if (attempt == maxRetries) {
                    Log.e(TAG, "All timeout attempts failed for: " + imageUrl);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image (attempt " + (attempt + 1) + "): " + e.getMessage(), e);
                if (attempt == maxRetries) {
                    Log.e(TAG, "All attempts failed for: " + imageUrl);
                }
            } finally {
                try {
                    if (input != null) input.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing image loading resources: " + e.getMessage());
                }
            }
        }
        
        return null; // All attempts failed
    }

    private boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        
        // Basic URL validation
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            Log.w(TAG, "Invalid URL protocol: " + imageUrl);
            return false;
        }
        
        // Check for common image extensions (optional, many APIs don't use extensions)
        String lowerUrl = imageUrl.toLowerCase();
        boolean hasImageExtension = lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") || 
                                   lowerUrl.contains(".png") || lowerUrl.contains(".webp") ||
                                   lowerUrl.contains(".gif") || lowerUrl.contains(".bmp");
        
        // Check for known placeholder services that might have issues
        if (imageUrl.contains("via.placeholder.com")) {
            Log.w(TAG, "Using placeholder service which may have DNS issues: " + imageUrl);
            // Still allow it, but log warning
        }
        
        // Basic domain validation
        try {
            java.net.URL url = new java.net.URL(imageUrl);
            String host = url.getHost();
            if (host == null || host.trim().isEmpty()) {
                Log.w(TAG, "Invalid hostname in URL: " + imageUrl);
                return false;
            }
        } catch (java.net.MalformedURLException e) {
            Log.e(TAG, "Malformed URL: " + imageUrl, e);
            return false;
        }
        
        return true;
    }

    private Bitmap resizeBitmapForWidget(Bitmap original) {
        if (original == null) return null;
        
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Widget optimal dimensions - giữ nguyên tỷ lệ khung hình
        int maxWidth = 400;
        int maxHeight = 600; // Tăng maxHeight để hỗ trợ tốt hơn ảnh dọc
        
        // Don't resize if already small enough
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            Log.d(TAG, "Image already optimal size: " + originalWidth + "x" + originalHeight);
            return original;
        }
        
        // Tính toán tỷ lệ để giữ nguyên aspect ratio
        float ratio = Math.min((float) maxWidth / originalWidth, (float) maxHeight / originalHeight);
        int newWidth = Math.round(originalWidth * ratio);
        int newHeight = Math.round(originalHeight * ratio);
        
        // Đảm bảo kích thước tối thiểu
        if (newWidth < 50) newWidth = 50;
        if (newHeight < 50) newHeight = 50;
        
        Log.d(TAG, "Resizing image while preserving aspect ratio from " + originalWidth + "x" + originalHeight + " to " + newWidth + "x" + newHeight + " (ratio: " + ratio + ")");
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            Log.d(TAG, "Network available: " + isAvailable);
            return isAvailable;
        } catch (Exception e) {
            Log.e(TAG, "Error checking network connectivity: " + e.getMessage());
            return true; // Assume network is available if we can't check
        }
    }
} 