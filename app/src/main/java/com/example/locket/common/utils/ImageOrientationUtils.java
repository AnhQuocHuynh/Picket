package com.example.locket.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.locket.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * 🔧 ImageOrientationUtils - Utility class để xử lý orientation của ảnh
 * Đảm bảo ảnh hiển thị đúng chiều trong toàn bộ ứng dụng
 */
public class ImageOrientationUtils {
    private static final String TAG = "ImageOrientationUtils";

    /**
     * Load ảnh với auto-rotation dựa trên EXIF
     */
    public static void loadImageWithCorrectOrientation(Context context, String imageUrl, ImageView imageView) {
        loadImageWithCorrectOrientation(context, imageUrl, imageView, true);
    }

    /**
     * Load ảnh với tùy chọn auto-rotation
     */
    public static void loadImageWithCorrectOrientation(Context context, String imageUrl, ImageView imageView, boolean maintainAspectRatio) {
        if (context == null || imageView == null) return;

        RequestOptions requestOptions;
        if (maintainAspectRatio) {
            requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_widget_empty_icon)
                    .error(R.drawable.ic_widget_empty_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter();
        } else {
            requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_widget_empty_icon)
                    .error(R.drawable.ic_widget_empty_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();
        }

        // Sử dụng CloudinaryImageLoader nếu là Cloudinary URL
        if (imageUrl != null && (imageUrl.contains("cloudinary.com") || imageUrl.contains("res.cloudinary.com"))) {
            if (maintainAspectRatio) {
                CloudinaryImageLoader.loadMomentImage(context, imageUrl, imageView);
            } else {
                CloudinaryImageLoader.loadImage(context, imageUrl, imageView);
            }
        } else {
            // Fallback cho các URL khác
            Glide.with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(imageView);
        }
    }

    /**
     * Kiểm tra và sửa orientation cho bitmap
     */
    public static Bitmap fixBitmapOrientation(Bitmap bitmap, Uri imageUri, Context context) {
        if (bitmap == null || imageUri == null || context == null) {
            return bitmap;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.w(TAG, "Cannot get input stream for EXIF reading");
                return bitmap;
            }

            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            inputStream.close();

            return rotateBitmapBasedOnOrientation(bitmap, orientation);
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data: " + e.getMessage());
            return bitmap;
        }
    }

    /**
     * Xoay bitmap dựa trên EXIF orientation
     */
    private static Bitmap rotateBitmapBasedOnOrientation(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.d(TAG, "🔄 Auto-rotating image 90 degrees");
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.d(TAG, "🔄 Auto-rotating image 180 degrees");
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.d(TAG, "🔄 Auto-rotating image 270 degrees");
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                Log.d(TAG, "🔄 Auto-flipping image horizontally");
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                Log.d(TAG, "🔄 Auto-flipping image vertically");
                matrix.setScale(1, -1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                Log.d(TAG, "🔄 Auto-transposing image");
                matrix.postRotate(90);
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                Log.d(TAG, "🔄 Auto-transversing image");
                matrix.postRotate(-90);
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                Log.d(TAG, "✅ Image orientation is already correct");
                return bitmap;
        }

        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, 
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            
            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }
            
            Log.d(TAG, "✅ Image orientation fixed successfully");
            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "❌ Out of memory when rotating bitmap: " + e.getMessage());
            return bitmap;
        }
    }

    /**
     * Lấy degrees rotation từ EXIF orientation
     */
    public static int getRotationDegreesFromOrientation(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Kiểm tra xem có cần rotation không
     */
    public static boolean needsRotation(int exifOrientation) {
        return exifOrientation != ExifInterface.ORIENTATION_NORMAL && 
               exifOrientation != ExifInterface.ORIENTATION_UNDEFINED;
    }

    /**
     * Log thông tin orientation để debug
     */
    public static void logOrientationInfo(Uri imageUri, Context context) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.d(TAG, "🔍 Cannot read orientation - input stream is null");
                return;
            }

            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            inputStream.close();

            Log.d(TAG, "🔍 Image EXIF orientation: " + getOrientationName(orientation) + " (" + orientation + ")");
            Log.d(TAG, "🔍 Needs rotation: " + needsRotation(orientation));
            Log.d(TAG, "🔍 Rotation degrees: " + getRotationDegreesFromOrientation(orientation));
        } catch (IOException e) {
            Log.e(TAG, "❌ Error reading EXIF for logging: " + e.getMessage());
        }
    }

    /**
     * Helper: Lấy tên orientation để debug
     */
    private static String getOrientationName(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return "NORMAL";
            case ExifInterface.ORIENTATION_ROTATE_90:
                return "ROTATE_90";
            case ExifInterface.ORIENTATION_ROTATE_180:
                return "ROTATE_180";
            case ExifInterface.ORIENTATION_ROTATE_270:
                return "ROTATE_270";
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return "FLIP_HORIZONTAL";
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return "FLIP_VERTICAL";
            case ExifInterface.ORIENTATION_TRANSPOSE:
                return "TRANSPOSE";
            case ExifInterface.ORIENTATION_TRANSVERSE:
                return "TRANSVERSE";
            default:
                return "UNDEFINED";
        }
    }
} 