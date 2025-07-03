package com.example.locket.common.repository;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.example.locket.common.database.AppDatabase;
import com.example.locket.common.database.dao.MomentDao;
import com.example.locket.common.database.entities.MomentEntity;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.post.Post;
import com.example.locket.common.models.post.PostsResponse;
import com.example.locket.common.models.post.CategoriesResponse;
import com.example.locket.common.network.PostApiService;
import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomentRepository {
    private static final String TAG = "MomentRepository";
    private final LiveData<List<MomentEntity>> allMoments;
    private final PostApiService postApiService;
    private final Context context;
    private final LoginResponse loginResponse;
    private final MomentDao momentDao;
    private final ExecutorService executor;

    public MomentRepository(Application application) {
        this.context = application;
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "moment_database")
                .fallbackToDestructiveMigration()
                .build();
        momentDao = db.momentDao();
        allMoments = momentDao.getAllMoments();
        postApiService = AuthApiClient.getAuthClient().create(PostApiService.class);
        loginResponse = SharedPreferencesUser.getLoginResponse(application);
        executor = Executors.newFixedThreadPool(2);
    }

    /**
     * 🔄 Refresh data from server - Fetch friends posts and convert to moments
     */
    public void refreshDataFromServer() {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            Log.e(TAG, "No authentication token available");
            return;
        }

        Log.d(TAG, "Fetching friends posts from server...");
        
        // Fetch friends posts with pagination
        Call<PostsResponse> call = postApiService.getFriendsPosts(authHeader, 1, 50); // Fetch first 50 posts

        call.enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostsResponse postsResponse = response.body();
                    if (postsResponse.isSuccess() && postsResponse.getData() != null) {
                        Log.d(TAG, "Successfully fetched " + postsResponse.getData().size() + " posts");
                        
                        // Convert posts to moment entities in background thread
                        executor.execute(() -> {
                            try {
                                List<MomentEntity> momentEntities = convertPostsToMoments(postsResponse.getData());
                                Log.d(TAG, "Converted " + momentEntities.size() + " posts to moments");
                                
                                // Clear old data and insert new data
                                Log.d(TAG, "Clearing old moments from database...");
                                momentDao.deleteAll();
                                
                                Log.d(TAG, "Inserting " + momentEntities.size() + " new moments...");
                                momentDao.insertAll(momentEntities);
                                
                                Log.d(TAG, "Successfully saved " + momentEntities.size() + " moments to database");
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving posts to database: " + e.getMessage(), e);
                                // Provide more specific error information
                                if (e.getMessage() != null && e.getMessage().contains("schema")) {
                                    Log.e(TAG, "Database schema mismatch detected. App restart may be required.");
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "API response unsuccessful: " + (postsResponse.getMessage() != null ? postsResponse.getMessage() : "Unknown error"));
                    }
                } else {
                    Log.e(TAG, "Error fetching posts: " + response.code() + " - " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PostsResponse> call, Throwable t) {
                Log.e(TAG, "Network error fetching posts: " + t.getMessage(), t);
            }
        });
    }

    /**
     * 🔄 Convert Post objects to MomentEntity objects for compatibility with existing UI
     */
    private List<MomentEntity> convertPostsToMoments(List<Post> posts) {
        List<MomentEntity> momentEntities = new ArrayList<>();
        
        for (Post post : posts) {
            try {
                MomentEntity entity = new MomentEntity();
                
                // Basic fields
                entity.id = post.getId();
                entity.user = post.getUser().getUsername();
                entity.imageUrl = post.getImageUrl();
                entity.caption = post.getCaption() != null ? post.getCaption() : "";
                entity.category = post.getCategory() != null ? post.getCategory() : "Khác";
                entity.timestamp = System.currentTimeMillis();
                
                // Convert createdAt to timestamp
                if (post.getCreatedAt() != null) {
                    try {
                        // Parse ISO 8601 date format: "2025-07-02T05:43:16.121Z"
                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        Date date = isoFormat.parse(post.getCreatedAt());
                        if (date != null) {
                            long serverTime = date.getTime();
                            long currentTime = System.currentTimeMillis();
                            
                            // 🎯 FIX: If server time is too far from current time (more than 30 minutes), 
                            // use current time instead to avoid timezone issues
                            long timeDiff = Math.abs(currentTime - serverTime);
                            if (timeDiff > 30 * 60 * 1000) { // 30 minutes
                                Log.w(TAG, "Server time differs by " + (timeDiff / 60000) + " minutes from local time, using local time instead");
                                entity.timestamp = currentTime;
                            } else {
                                entity.timestamp = serverTime;
                            }
                        }
                    } catch (ParseException e) {
                        Log.w(TAG, "Failed to parse date: " + post.getCreatedAt(), e);
                        // Keep current timestamp as fallback
                    }
                }
                
                // Set thumbnail URL (same as image URL for now)
                entity.thumbnailUrl = post.getImageUrl();
                
                // Create overlays for caption if exists
                if (post.getCaption() != null && !post.getCaption().trim().isEmpty()) {
                    List<MomentEntity.Overlay> overlays = new ArrayList<>();
                    MomentEntity.Overlay overlay = new MomentEntity.Overlay();
                    overlay.overlay_id = "caption:text"; // Default caption type
                    overlay.alt_text = post.getCaption();
                    overlays.add(overlay);
                    entity.overlays = overlays;
                }
                
                // Calculate date seconds for compatibility
                entity.dateSeconds = entity.timestamp / 1000;
                
                momentEntities.add(entity);
                Log.d(TAG, "Converted post to moment: " + post.getUser().getUsername() + " - " + post.getCaption());
                
            } catch (Exception e) {
                Log.e(TAG, "Error converting post to moment: " + post.getId(), e);
            }
        }
        
        return momentEntities;
    }

    /**
     * 🆕 Add new moment locally after successful post
     * This ensures immediate display with correct local time
     */
    public void addNewMomentLocally(String imageUrl, String caption, String currentUserName) {
        executor.execute(() -> {
            try {
                MomentEntity entity = new MomentEntity();
                
                // Generate temporary ID for local moment
                entity.id = "local_" + System.currentTimeMillis();
                entity.canonicalUid = entity.id;
                entity.user = currentUserName != null ? currentUserName : "You";
                entity.imageUrl = imageUrl;
                entity.thumbnailUrl = imageUrl;
                entity.caption = caption != null ? caption : "";
                entity.category = "Mới đăng";
                
                // 🎯 KEY FIX: Use current local time for immediate display
                long currentTime = System.currentTimeMillis();
                entity.timestamp = currentTime;
                entity.dateSeconds = currentTime / 1000;
                
                // Create overlays for caption if exists
                if (caption != null && !caption.trim().isEmpty()) {
                    List<MomentEntity.Overlay> overlays = new ArrayList<>();
                    MomentEntity.Overlay overlay = new MomentEntity.Overlay();
                    overlay.overlay_id = "caption:text";
                    overlay.alt_text = caption;
                    overlays.add(overlay);
                    entity.overlays = overlays;
                }
                
                // Insert to database
                momentDao.insert(entity);
                Log.d(TAG, "✅ Added new moment locally with current time: " + entity.user + " - " + entity.caption);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error adding moment locally: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 🏷️ Fetch available categories from API
     */
    public interface CategoriesCallback {
        void onCategoriesReceived(List<CategoriesResponse.CategoryData> categories);
        void onError(String error);
    }

    public void fetchAvailableCategories(CategoriesCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            Log.e(TAG, "No authentication token available for categories");
            callback.onError("Authentication required");
            return;
        }

        Log.d(TAG, "Fetching available categories from server...");
        
        Call<CategoriesResponse> call = postApiService.getAvailableCategories(authHeader);
        call.enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoriesResponse categoriesResponse = response.body();
                    if (categoriesResponse.isSuccess() && categoriesResponse.getData() != null) {
                        Log.d(TAG, "Successfully fetched " + categoriesResponse.getData().size() + " categories");
                        callback.onCategoriesReceived(categoriesResponse.getData());
                    } else {
                        String errorMsg = "API response unsuccessful: " + 
                            (categoriesResponse.getMessage() != null ? categoriesResponse.getMessage() : "Unknown error");
                        Log.e(TAG, errorMsg);
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "Error fetching categories: " + response.code() + " - " + response.message();
                    Log.e(TAG, errorMsg);
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error body", e);
                        }
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                String errorMsg = "Network error fetching categories: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * 🧪 Test database connection and schema
     */
    public void testDatabaseConnection() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Testing database connection...");
                
                // Test basic operations
                List<MomentEntity> currentMoments = momentDao.getAllMomentsSync();
                Log.d(TAG, "Current moments in database: " + currentMoments.size());
                
                // Test insert a dummy moment
                MomentEntity testMoment = new MomentEntity();
                testMoment.id = "test_" + System.currentTimeMillis();
                testMoment.user = "test_user";
                testMoment.imageUrl = "test_url";
                testMoment.caption = "Test moment";
                testMoment.timestamp = System.currentTimeMillis();
                testMoment.dateSeconds = testMoment.timestamp / 1000;
                
                momentDao.insert(testMoment);
                Log.d(TAG, "Successfully inserted test moment");
                
                // Remove test moment
                momentDao.deleteById(testMoment.id);
                Log.d(TAG, "Database connection test completed successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Database connection test failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 📋 Get all moments (converted from posts)
     */
    public LiveData<List<MomentEntity>> getAllMoments() {
        return allMoments;
    }

    /**
     * 🧹 Cleanup resources
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

