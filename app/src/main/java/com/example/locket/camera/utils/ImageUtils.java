package com.example.locket.camera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static Uri processImage(Context context, Uri imageUri, int quality) throws IOException {
        Bitmap bitmap = getBitmapFromUri(context, imageUri);

        if (bitmap == null) {
            throw new IOException("Không thể đọc ảnh từ Uri");
        }

        // 🔧 FIX: Xử lý EXIF orientation cho ảnh từ gallery
        bitmap = fixImageOrientation(context, imageUri, bitmap);

        // Lưu ảnh đã xoay đúng chiều
        return saveBitmapToCache(context, bitmap, quality);
    }

    private static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        try (ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
             FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor())) {
            return BitmapFactory.decodeStream(fileInputStream);
        }
    }

    public static Uri saveBitmapToCache(Context context, Bitmap bitmap, int quality) throws IOException {
        File file = new File(context.getCacheDir(), "processed_image.jpg");
        try (OutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }
        return Uri.fromFile(file);
    }

    /**
     * 🔧 FIX: Xử lý EXIF orientation cho ảnh từ gallery
     */
    private static Bitmap fixImageOrientation(Context context, Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.w(TAG, "Cannot get input stream for EXIF reading");
                return bitmap;
            }

            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            inputStream.close();

            return rotateBitmap(bitmap, orientation);
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data: " + e.getMessage());
            return bitmap;
        }
    }

    /**
     * 🔧 FIX: Xoay bitmap theo EXIF orientation
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.d(TAG, "🔄 Rotating image 90 degrees");
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.d(TAG, "🔄 Rotating image 180 degrees");
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.d(TAG, "🔄 Rotating image 270 degrees");
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                Log.d(TAG, "🔄 Flipping image horizontally");
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                Log.d(TAG, "🔄 Flipping image vertically");
                matrix.setScale(1, -1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                Log.d(TAG, "🔄 Transposing image");
                matrix.postRotate(90);
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                Log.d(TAG, "🔄 Transversing image");
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

            // Recycle original bitmap if it's different from rotated one
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
     * 🔧 FIX: Xử lý rotation cho ảnh từ camera (ImageProxy)
     * Camera thường có rotation khác với orientation của device
     */
    public static Bitmap fixCameraImageRotation(Bitmap bitmap, int rotationDegrees, boolean isFrontCamera) {
        if (bitmap == null) return null;

        Matrix matrix = new Matrix();

        // Xoay theo rotation của camera
        if (rotationDegrees != 0) {
            Log.d(TAG, "🔄 Rotating camera image by " + rotationDegrees + " degrees");
            matrix.postRotate(rotationDegrees);
        }

        // Flip horizontal cho front camera để có hiệu ứng gương
        if (isFrontCamera) {
            Log.d(TAG, "🪞 Flipping front camera image horizontally");
            matrix.postScale(-1, 1);
        }

        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

            Log.d(TAG, "✅ Camera image rotation fixed successfully");
            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "❌ Out of memory when fixing camera rotation: " + e.getMessage());
            return bitmap;
        }
    }

    /**
     * 🔧 Helper method: Lấy rotation degrees từ EXIF orientation
     */
    public static int getRotationFromExif(int exifOrientation) {
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
}

