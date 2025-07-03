package com.example.locket.chat;

import android.content.Context;
import android.util.Log;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.models.auth.LoginResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Helper class to map usernames to user IDs for chat functionality
 * This implementation queries the friends list to get proper Firebase user IDs
 */
public class UserMappingHelper {
    private static final String TAG = "UserMappingHelper";
    
    // Cache for username to user ID mapping
    private static final Map<String, String> usernameToUserIdMap = new HashMap<>();
    private static volatile boolean isCacheInitialized = false;
    private static Context appContext;
    
    /**
     * Initialize the helper with application context
     * @param context Application context
     */
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        // Preload cache in background
        loadUserMappingsAsync();
    }
    
    /**
     * Get user ID from username with immediate response
     * @param username The username to lookup
     * @return User ID if found in cache, or username as fallback
     */
    public static String getUserIdFromUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        // Try to get from cache first
        String userId = usernameToUserIdMap.get(username);
        if (userId != null) {
            Log.d(TAG, "✅ Found cached mapping: " + username + " -> " + userId);
            return userId;
        }
        
        // If cache is not initialized, try to initialize it
        if (!isCacheInitialized && appContext != null) {
            loadUserMappingsAsync();
        }
        
        // Fallback to username for backward compatibility
        Log.w(TAG, "⚠️ No mapping found for username: " + username + ", using username as fallback");
        return username;
    }
    
    /**
     * Get user ID from username with async callback
     * @param username The username to lookup
     * @param callback Callback with result
     */
    public static void getUserIdFromUsernameAsync(String username, UserIdCallback callback) {
        if (username == null || username.trim().isEmpty()) {
            if (callback != null) callback.onResult(null);
            return;
        }
        
        // Check cache first
        String cachedUserId = usernameToUserIdMap.get(username);
        if (cachedUserId != null) {
            Log.d(TAG, "✅ Found cached mapping: " + username + " -> " + cachedUserId);
            if (callback != null) callback.onResult(cachedUserId);
            return;
        }
        
        // Load from API and then return result
        if (appContext != null) {
            loadUserMappingsAsync(() -> {
                String userId = usernameToUserIdMap.get(username);
                if (userId != null) {
                    Log.d(TAG, "✅ Found mapping after refresh: " + username + " -> " + userId);
                } else {
                    Log.w(TAG, "⚠️ No mapping found for username: " + username + " after refresh, using username as fallback");
                    userId = username;
                }
                if (callback != null) callback.onResult(userId);
            });
        } else {
            Log.w(TAG, "⚠️ No context available, using username as fallback");
            if (callback != null) callback.onResult(username);
        }
    }
    
    /**
     * Load user mappings from friends list asynchronously
     */
    private static void loadUserMappingsAsync() {
        loadUserMappingsAsync(null);
    }
    
    /**
     * Load user mappings from friends list asynchronously with callback
     * @param onComplete Callback when loading is complete
     */
    private static void loadUserMappingsAsync(Runnable onComplete) {
        if (appContext == null) {
            Log.e(TAG, "❌ App context is null, cannot load user mappings");
            if (onComplete != null) onComplete.run();
            return;
        }
        
        Log.d(TAG, "🔄 Loading user mappings from friends list...");
        
        new Thread(() -> {
            try {
                FriendshipRepository friendshipRepository = new FriendshipRepository(appContext);
                
                friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
                    @Override
                    public void onSuccess(FriendsListResponse response) {
                        if (response != null && response.getData() != null) {
                            synchronized (usernameToUserIdMap) {
                                usernameToUserIdMap.clear();
                                
                                for (FriendsListResponse.FriendData friend : response.getData()) {
                                    if (friend.getUsername() != null && friend.getId() != null) {
                                        usernameToUserIdMap.put(friend.getUsername(), friend.getId());
                                        Log.d(TAG, "📝 Cached mapping: " + friend.getUsername() + " -> " + friend.getId());
                                    }
                                }
                                
                                // Also add current user mapping
                                LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(appContext);
                                if (loginResponse != null && loginResponse.getUser() != null) {
                                    String currentUsername = loginResponse.getUser().getUsername();
                                    String currentUserId = loginResponse.getLocalId();
                                    if (currentUsername != null && currentUserId != null) {
                                        usernameToUserIdMap.put(currentUsername, currentUserId);
                                        Log.d(TAG, "📝 Cached current user mapping: " + currentUsername + " -> " + currentUserId);
                                    }
                                }
                                
                                isCacheInitialized = true;
                                Log.d(TAG, "✅ Successfully loaded " + usernameToUserIdMap.size() + " user mappings");
                            }
                        } else {
                            Log.w(TAG, "⚠️ Friends list response is null or empty");
                            isCacheInitialized = true; // Mark as initialized even if empty
                        }
                        
                        if (onComplete != null) onComplete.run();
                    }
                    
                    @Override
                    public void onError(String message, int code) {
                        Log.e(TAG, "❌ Failed to load friends list for user mapping: " + message + " (code: " + code + ")");
                        isCacheInitialized = true; // Mark as initialized to avoid repeated attempts
                        if (onComplete != null) onComplete.run();
                    }
                    
                    @Override
                    public void onLoading(boolean isLoading) {
                        Log.d(TAG, "🔄 Loading friends list: " + isLoading);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Exception while loading user mappings: " + e.getMessage(), e);
                isCacheInitialized = true;
                if (onComplete != null) onComplete.run();
            }
        }).start();
    }
    
    /**
     * Add username to user ID mapping to cache manually
     * @param username The username
     * @param userId The corresponding user ID
     */
    public static void addUserMapping(String username, String userId) {
        if (username != null && userId != null) {
            synchronized (usernameToUserIdMap) {
                usernameToUserIdMap.put(username, userId);
            }
            Log.d(TAG, "📝 Manually added user mapping: " + username + " -> " + userId);
        }
    }
    
    /**
     * Force refresh the mapping cache
     */
    public static void refreshCache() {
        Log.d(TAG, "🔄 Force refreshing user mapping cache...");
        isCacheInitialized = false;
        synchronized (usernameToUserIdMap) {
            usernameToUserIdMap.clear();
        }
        loadUserMappingsAsync();
    }
    
    /**
     * Clear the mapping cache
     */
    public static void clearCache() {
        synchronized (usernameToUserIdMap) {
            usernameToUserIdMap.clear();
        }
        isCacheInitialized = false;
        Log.d(TAG, "🗑️ Cleared user mapping cache");
    }
    
    /**
     * Check if we have a mapping for the given username
     * @param username The username to check
     * @return true if mapping exists
     */
    public static boolean hasMapping(String username) {
        return username != null && usernameToUserIdMap.containsKey(username);
    }
    
    /**
     * Get all cached mappings (for debugging)
     * @return Map of username to user ID
     */
    public static Map<String, String> getAllMappings() {
        return new HashMap<>(usernameToUserIdMap);
    }
    
    /**
     * Check if cache is initialized
     * @return true if cache has been loaded
     */
    public static boolean isCacheInitialized() {
        return isCacheInitialized;
    }
    
    /**
     * Callback interface for async user ID lookup
     */
    public interface UserIdCallback {
        void onResult(String userId);
    }
} 