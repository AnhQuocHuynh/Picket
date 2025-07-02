package com.example.locket.feed.widget.worker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.locket.feed.widget.service.LocketWidgetService;
import com.example.locket.feed.widget.network.NetworkManager;
import com.example.locket.common.models.post.Post;
import com.example.locket.common.utils.WidgetUpdateHelper;
import java.util.List;

public class DataSyncWorker extends Worker {
    private static final String TAG = "DataSyncWorker";
    private static final String PREFS_NAME = "widget_prefs";
    private static final String LAST_POST_ID = "last_post_id";
    private static final String LAST_SYNC_TIME = "last_sync_time";

    public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Performing background data sync for widget");
            Context context = getApplicationContext();
            
            // Check if user is still logged in
            if (!WidgetUpdateHelper.isUserLoggedIn(context)) {
                Log.w(TAG, "User not logged in, skipping sync");
                return Result.success();
            }
            
            // Record sync attempt
            saveLastSyncTime(context);
            
            // Check for new posts
            checkForNewPosts(context);
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error during data sync: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    private void checkForNewPosts(Context context) {
        NetworkManager networkManager = new NetworkManager(context);
        
        networkManager.getLatestPosts(new NetworkManager.ApiCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                Log.d(TAG, "Background sync: received " + posts.size() + " posts");
                
                if (!posts.isEmpty()) {
                    Post latestPost = posts.get(0);
                    String currentPostId = latestPost.getId();
                    String lastKnownPostId = getLastPostId(context);
                    
                    // Check if this is a new post
                    boolean isNewPost = lastKnownPostId == null || !lastKnownPostId.equals(currentPostId);
                    
                    if (isNewPost) {
                        Log.d(TAG, "New post detected! Updating widget immediately");
                        saveLastPostId(context, currentPostId);
                        
                        // Trigger immediate widget update
                        Intent serviceIntent = new Intent(context, LocketWidgetService.class);
                        serviceIntent.setAction("ACTION_UPDATE_WIDGET");
                        context.startService(serviceIntent);
                    } else {
                        Log.d(TAG, "No new posts since last check");
                    }
                } else {
                    Log.d(TAG, "No posts available");
                    
                    // Still trigger update to show "no posts" state if needed
                    Intent serviceIntent = new Intent(context, LocketWidgetService.class);
                    serviceIntent.setAction("ACTION_UPDATE_WIDGET");
                    context.startService(serviceIntent);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Background sync error: " + error);
                
                // On error, still try to update widget in case of temporary network issues
                Intent serviceIntent = new Intent(context, LocketWidgetService.class);
                serviceIntent.setAction("ACTION_UPDATE_WIDGET");
                context.startService(serviceIntent);
            }
        });
    }

    private String getLastPostId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LAST_POST_ID, null);
    }

    private void saveLastPostId(Context context, String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(LAST_POST_ID, postId).apply();
        Log.d(TAG, "Saved last post ID: " + postId);
    }

    private void saveLastSyncTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(LAST_SYNC_TIME, System.currentTimeMillis()).apply();
    }

    public static long getLastSyncTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(LAST_SYNC_TIME, 0);
    }
} 