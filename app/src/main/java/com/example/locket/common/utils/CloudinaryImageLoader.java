package com.example.locket.common.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.locket.R;

/**
 * CloudinaryImageLoader - Utility để optimize việc load ảnh từ Cloudinary
 * Supports transformations và caching cho Cloudinary URLs
 */
public class CloudinaryImageLoader {

    /**
     * Load ảnh với optimization cơ bản
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.ic_widget_empty_icon);
    }

    /**
     * Load ảnh với custom placeholder
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderRes) {
        if (context == null || imageView == null) return;

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();

        Glide.with(context)
                .load(optimizeCloudinaryUrl(imageUrl))
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Load ảnh thumbnail với kích thước nhỏ (cho danh sách)
     */
    public static void loadThumbnail(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_widget_empty_icon)
                .error(R.drawable.ic_widget_empty_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(200, 200) // Giới hạn kích thước cho thumbnail
                .centerCrop();

        Glide.with(context)
                .load(optimizeCloudinaryUrl(imageUrl, "w_200,h_200,c_fill"))
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Load ảnh profile với transformation tròn
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_widget_empty_icon)
                .error(R.drawable.ic_widget_empty_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop();

        Glide.with(context)
                .load(optimizeCloudinaryUrl(imageUrl, "w_150,h_150,c_fill"))
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Load ảnh full size cho preview - giữ nguyên tỷ lệ
     */
    public static void loadFullImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_widget_empty_icon)
                .error(R.drawable.ic_widget_empty_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter();

        Glide.with(context)
                .load(optimizeCloudinaryUrl(imageUrl, "q_auto,f_auto"))
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * 🔧 NEW: Load ảnh moment với fitCenter để giữ nguyên tỷ lệ
     */
    public static void loadMomentImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_widget_empty_icon)
                .error(R.drawable.ic_widget_empty_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter();

        Glide.with(context)
                .load(optimizeCloudinaryUrl(imageUrl, "q_auto,f_auto,c_scale"))
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Optimize Cloudinary URL với transformations
     */
    private static String optimizeCloudinaryUrl(String originalUrl) {
        return optimizeCloudinaryUrl(originalUrl, "q_auto,f_auto");
    }

    /**
     * Optimize Cloudinary URL với custom transformations
     */
    private static String optimizeCloudinaryUrl(String originalUrl, String transformations) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }

        // Chỉ optimize nếu là Cloudinary URL
        if (!originalUrl.contains("cloudinary.com") && !originalUrl.contains("res.cloudinary.com")) {
            return originalUrl;
        }

        try {
            // Pattern: https://res.cloudinary.com/cloud-name/image/upload/...
            if (originalUrl.contains("/image/upload/")) {
                // Thêm transformations vào URL
                String optimizedUrl = originalUrl.replace("/image/upload/", "/image/upload/" + transformations + "/");
                return optimizedUrl;
            }
        } catch (Exception e) {
            // Nếu có lỗi, trả về URL gốc
            return originalUrl;
        }

        return originalUrl;
    }

    /**
     * Preload ảnh vào cache
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (context == null || imageUrl == null || imageUrl.isEmpty()) return;

        Glide.with(context)
                .load(optimizeCloudinaryUrl(imageUrl))
                .preload();
    }

    /**
     * Clear cache cho một URL cụ thể
     */
    public static void clearImageCache(Context context, String imageUrl) {
        if (context == null || imageUrl == null || imageUrl.isEmpty()) return;

        Glide.with(context)
                .clear((Target<?>) Glide.with(context).load(imageUrl));
    }
} 