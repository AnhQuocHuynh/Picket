package com.example.locket.feed.widget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.locket.feed.widget.utils.ImageLoader;
import com.example.locket.feed.widget.service.LocketWidgetService;
import com.example.locket.MainActivity;
import com.example.locket.R;
import com.example.locket.common.models.post.Post;
import com.example.locket.common.utils.WidgetUpdateHelper;
import com.example.locket.feed.widget.worker.DataSyncWorker;

import java.util.concurrent.TimeUnit;

public class LocketWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "LocketWidgetProvider";
    private static final String WORK_NAME = "locket-widget-update";
    private static final String ACTION_REFRESH = "com.example.locket.WIDGET_REFRESH";
    private static ImageLoader imageLoader;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate called for " + appWidgetIds.length + " widgets");
        if (imageLoader == null) {
            imageLoader = new ImageLoader();
        }
        // Cập nhật tất cả widget instances
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, null);
        }
        // Khởi động service để fetch data
        Intent serviceIntent = new Intent(context, LocketWidgetService.class);
        serviceIntent.setAction("ACTION_UPDATE_WIDGET");
        context.startService(serviceIntent);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled called - First widget instance added");
        // Widget được thêm lần đầu
        scheduleWidgetUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled called - Last widget instance removed");
        // Widget cuối cùng bị xóa
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling work: " + e.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d(TAG, "onReceive called with action: " + action);
        
        if (ACTION_REFRESH.equals(action)) {
            // Manual refresh triggered
            Intent serviceIntent = new Intent(context, LocketWidgetService.class);
            serviceIntent.setAction("ACTION_UPDATE_WIDGET");
            context.startService(serviceIntent);
        } else if (WidgetUpdateHelper.ACTION_USER_CHANGED.equals(action)) {
            // 🔄 User changed (login/logout) - immediate widget update
            Log.d(TAG, "User changed detected - triggering immediate widget update");
            Intent serviceIntent = new Intent(context, LocketWidgetService.class);
            serviceIntent.setAction("ACTION_UPDATE_WIDGET");
            context.startService(serviceIntent);
            
            // Also trigger onUpdate to refresh all widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, LocketWidgetProvider.class));
            if (appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, LocketWidgetProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Post latestPost) {
        Log.d(TAG, "Updating widget ID: " + appWidgetId);
        
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);
            
            // Set up click intent to open app
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            
            // Set up refresh button (if exists in layout)
            Intent refreshIntent = new Intent(context, LocketWidgetProvider.class);
            refreshIntent.setAction(ACTION_REFRESH);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            if (latestPost != null && latestPost.getUser() != null) {
                Log.d(TAG, "Updating widget with post from: " + latestPost.getUser().getUsername());
                
                // Cập nhật UI với dữ liệu từ Post
                views.setTextViewText(R.id.friend_name, latestPost.getUser().getUsername());
                
                // Handle caption - show "No caption" if empty
                String caption = latestPost.getCaption();
                if (caption == null || caption.trim().isEmpty()) {
                    caption = "📸 New photo";
                }
                views.setTextViewText(R.id.post_content, caption);
                
                // First update widget with text content
                appWidgetManager.updateAppWidget(appWidgetId, views);
                
                // Initialize ImageLoader if needed
                if (imageLoader == null) {
                    imageLoader = new ImageLoader();
                }
                
                // Load avatar image asynchronously
                if (latestPost.getUser().getProfilePicture() != null && !latestPost.getUser().getProfilePicture().isEmpty()) {
                    Log.d(TAG, "Loading avatar image: " + latestPost.getUser().getProfilePicture());
                    imageLoader.loadImageIntoWidget(context, latestPost.getUser().getProfilePicture(), 
                        appWidgetId, R.id.friend_avatar, appWidgetManager);
                } else {
                    Log.d(TAG, "No avatar URL, using default avatar");
                    views.setImageViewResource(R.id.friend_avatar, R.drawable.default_avatar);
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
                }
                
                // Load post image asynchronously
                if (latestPost.getImageUrl() != null && !latestPost.getImageUrl().isEmpty()) {
                    Log.d(TAG, "Loading post image: " + latestPost.getImageUrl());
                    imageLoader.loadImageIntoWidget(context, latestPost.getImageUrl(), 
                        appWidgetId, R.id.post_image, appWidgetManager);
                } else {
                    Log.d(TAG, "No post image URL, using default background");
                    views.setImageViewResource(R.id.post_image, R.drawable.widget_background);
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
                }
            } else {
                Log.d(TAG, "No post data available, showing placeholder");
                
                // Hiển thị placeholder khi không có data
                views.setTextViewText(R.id.friend_name, "No friends posts");
                views.setTextViewText(R.id.post_content, "Add friends to see their posts here");
                // Set default images
                views.setImageViewResource(R.id.friend_avatar, R.drawable.default_avatar);
                views.setImageViewResource(R.id.post_image, R.drawable.widget_background);
                
                // Update widget with placeholder content
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            
            Log.d(TAG, "Widget text content updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget: " + e.getMessage(), e);
            
            // Show error state in widget
            try {
                RemoteViews errorViews = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);
                errorViews.setTextViewText(R.id.friend_name, "Error");
                errorViews.setTextViewText(R.id.post_content, "Tap to retry");
                errorViews.setImageViewResource(R.id.friend_avatar, R.drawable.default_avatar);
                errorViews.setImageViewResource(R.id.post_image, R.drawable.widget_background);
                
                // Set up click intent to retry
                Intent intent = new Intent(context, LocketWidgetProvider.class);
                intent.setAction(ACTION_REFRESH);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                errorViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
                
                appWidgetManager.updateAppWidget(appWidgetId, errorViews);
            } catch (Exception innerException) {
                Log.e(TAG, "Error showing error state: " + innerException.getMessage());
            }
        }
    }

    private void scheduleWidgetUpdate(Context context) {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Use shorter interval for more frequent updates (5 minutes instead of 15)
            long updateInterval = 5 * 60 * 1000; // 5 minutes in milliseconds
            long flexInterval = 2 * 60 * 1000;   // 2 minutes flex

            PeriodicWorkRequest updateRequest = new PeriodicWorkRequest.Builder(
                    DataSyncWorker.class,
                    updateInterval,
                    TimeUnit.MILLISECONDS,
                    flexInterval,
                    TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                            WORK_NAME,
                            androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                            updateRequest);

            Log.d(TAG, "Scheduled periodic widget update every 5 minutes");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling widget update: " + e.getMessage(), e);
        }
    }
} 