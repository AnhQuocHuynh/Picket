package com.example.locket.feed.widget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.locket.common.models.post.Post;
import com.example.locket.feed.widget.network.NetworkManager;
import com.example.locket.feed.widget.provider.LocketWidgetProvider;

import java.util.List;

public class LocketWidgetService extends Service {
    private static final String TAG = "LocketWidgetService";
    private NetworkManager networkManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        networkManager = new NetworkManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started with action: " + (intent != null ? intent.getAction() : "null"));
        if (intent != null && "ACTION_UPDATE_WIDGET".equals(intent.getAction())) {
            fetchLatestPosts();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    private void fetchLatestPosts() {
        Log.d(TAG, "Fetching latest friends posts (excluding own posts)");
        
        networkManager.getLatestPosts(new NetworkManager.ApiCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                Log.d(TAG, "Successfully fetched " + posts.size() + " friends' posts");
                
                if (!posts.isEmpty()) {
                    Post latestPost = posts.get(0);
                    Log.d(TAG, "Latest friend's post from: " + latestPost.getUser().getUsername());
                    Log.d(TAG, "Post content: " + latestPost.getCaption());
                    updateAllWidgets(latestPost);
                } else {
                    Log.d(TAG, "No friends' posts found - user may not have friends or friends haven't posted yet");
                    updateAllWidgets(null);
                }
                
                // Stop service after processing
                stopSelf();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching friends' posts: " + error);
                
                // Categorize error types for better user feedback
                if (error.contains("authentication") || error.contains("login")) {
                    Log.w(TAG, "Authentication error - user needs to login");
                } else if (error.contains("Cannot connect") || error.contains("ConnectException")) {
                    Log.w(TAG, "Network connectivity issue");
                } else if (error.contains("No posts found") || error.contains("No friends")) {
                    Log.i(TAG, "No content available - this is normal for new users");
                } else {
                    Log.w(TAG, "Other error: " + error);
                }
                
                // Update widgets with null to show appropriate placeholder
                updateAllWidgets(null);
                
                // Stop service after processing
                stopSelf();
            }
        });
    }

    private void updateAllWidgets(Post latestPost) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, LocketWidgetProvider.class));
        
        Log.d(TAG, "Updating " + appWidgetIds.length + " widget instances");
        
        if (appWidgetIds.length == 0) {
            Log.w(TAG, "No widget instances found to update");
            return;
        }
        
        for (int appWidgetId : appWidgetIds) {
            try {
                LocketWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId, latestPost);
                Log.d(TAG, "Successfully updated widget ID: " + appWidgetId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating widget ID " + appWidgetId + ": " + e.getMessage(), e);
            }
        }
        
        Log.d(TAG, "All widgets update process completed");
    }
} 