package com.example.locket.chat;

import android.content.Context;
import android.util.Log;

import com.example.locket.chat.model.ChatMessage;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MessageThreadManager {
    private static final String TAG = "MessageThreadManager";
    private static final String DATABASE_URL = "https://picket-se104-default-rtdb.asia-southeast1.firebasedatabase.app";
    
    private final Context context;
    private final DatabaseReference databaseReference;
    private final String currentUserId;

    public MessageThreadManager(Context context) {
        this.context = context;
        this.currentUserId = SharedPreferencesUser.getLoginResponse(context).getLocalId();
        this.databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("chat_rooms");
        
        // Initialize UserMappingHelper if not already initialized
        UserMappingHelper.initialize(context);
        
        Log.d(TAG, "🔧 MessageThreadManager initialized with currentUserId: " + currentUserId);
    }

    /**
     * Create or get existing chat room ID between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Chat room ID string
     */
    public String getChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) > 0) {
            return userId2 + "_" + userId1;
        } else {
            return userId1 + "_" + userId2;
        }
    }

    /**
     * Create and send a moment comment message to the appropriate chat thread
     * @param commentText The comment text
     * @param momentId ID of the moment being commented on
     * @param momentImageUrl URL of the moment image
     * @param momentOwnerId ID of the moment owner
     * @param callback Callback for success/failure
     */
    public void createMomentCommentMessage(String commentText, String momentId, 
                                         String momentImageUrl, String momentOwnerId,
                                         MomentCommentCallback callback) {
        
        Log.d(TAG, "🚀 DEBUG_STEP_1: Starting moment comment message creation");
        Log.d(TAG, "📝 Comment text: " + commentText);
        Log.d(TAG, "🆔 Moment ID: " + momentId);
        Log.d(TAG, "🖼️ Moment image URL: " + momentImageUrl);
        Log.d(TAG, "👤 Moment owner ID: " + momentOwnerId);
        Log.d(TAG, "👤 Current user ID: " + currentUserId);
        
        // Validation checks
        if (commentText == null || commentText.trim().isEmpty()) {
            String error = "Comment text is null or empty";
            Log.e(TAG, "❌ DEBUG_STEP_1_FAILED: " + error);
            if (callback != null) callback.onError(error);
            return;
        }
        
        if (momentId == null || momentId.trim().isEmpty()) {
            String error = "Moment ID is null or empty";
            Log.e(TAG, "❌ DEBUG_STEP_1_FAILED: " + error);
            if (callback != null) callback.onError(error);
            return;
        }
        
        if (momentOwnerId == null || momentOwnerId.trim().isEmpty()) {
            String error = "Moment owner ID is null or empty";
            Log.e(TAG, "❌ DEBUG_STEP_1_FAILED: " + error);
            if (callback != null) callback.onError(error);
            return;
        }
        
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            String error = "Current user ID is null or empty - user not authenticated";
            Log.e(TAG, "❌ DEBUG_STEP_1_FAILED: " + error);
            if (callback != null) callback.onError(error);
            return;
        }
        
        // Don't create message if commenting on own moment
        if (currentUserId.equals(momentOwnerId)) {
            Log.d(TAG, "ℹ️ DEBUG_STEP_1_SKIP: Not creating message for comment on own moment");
            if (callback != null) callback.onSuccess();
            return;
        }
        
        Log.d(TAG, "✅ DEBUG_STEP_1_SUCCESS: Validation passed, proceeding to create message");

        try {
            Log.d(TAG, "🚀 DEBUG_STEP_2: Creating chat room ID");
            
            // Get chat room ID
            String chatRoomId = getChatRoomId(currentUserId, momentOwnerId);
            Log.d(TAG, "✅ DEBUG_STEP_2_SUCCESS: Chat room ID created: " + chatRoomId);
            
            Log.d(TAG, "🚀 DEBUG_STEP_3: Creating moment comment message object");
            
            // Create moment comment message
            ChatMessage momentCommentMessage = new ChatMessage(
                commentText,
                currentUserId,
                momentOwnerId,
                System.currentTimeMillis(),
                momentId,
                momentImageUrl,
                momentOwnerId
            );
            
            Log.d(TAG, "✅ DEBUG_STEP_3_SUCCESS: Message object created");
            Log.d(TAG, "📋 Message details:");
            Log.d(TAG, "   - Type: " + momentCommentMessage.getType());
            Log.d(TAG, "   - Text: " + momentCommentMessage.getText());
            Log.d(TAG, "   - Sender: " + momentCommentMessage.getSenderId());
            Log.d(TAG, "   - Receiver: " + momentCommentMessage.getReceiverId());
            Log.d(TAG, "   - Timestamp: " + momentCommentMessage.getTimestamp());
            Log.d(TAG, "   - Moment ID: " + momentCommentMessage.getMomentId());
            Log.d(TAG, "   - Moment Image URL: " + momentCommentMessage.getMomentImageUrl());

            Log.d(TAG, "🚀 DEBUG_STEP_4: Sending message to Firebase");
            
            // Send to Firebase
            DatabaseReference chatRoomRef = databaseReference.child(chatRoomId);
            chatRoomRef.push().setValue(momentCommentMessage)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ DEBUG_STEP_4_SUCCESS: Moment comment message sent successfully to Firebase");
                        Log.d(TAG, "🎯 Chat room: " + chatRoomId);
                        Log.d(TAG, "📬 Message will appear in ChatActivity for both users");
                        
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        String error = "Failed to send to Firebase: " + e.getMessage();
                        Log.e(TAG, "❌ DEBUG_STEP_4_FAILED: " + error, e);
                        Log.e(TAG, "🔍 Firebase error details:");
                        Log.e(TAG, "   - Database URL: " + DATABASE_URL);
                        Log.e(TAG, "   - Chat room path: chat_rooms/" + chatRoomId);
                        Log.e(TAG, "   - Error type: " + e.getClass().getSimpleName());
                        
                        if (callback != null) callback.onError(error);
                    });

        } catch (Exception e) {
            String error = "Exception during message creation: " + e.getMessage();
            Log.e(TAG, "❌ DEBUG_STEP_GENERAL_ERROR: " + error, e);
            if (callback != null) callback.onError(error);
        }
    }

    /**
     * Extract moment owner ID from current visible moment with enhanced debugging
     * @param currentMoment The current moment entity
     * @return User ID of the moment owner
     */
    public String extractMomentOwnerId(Object currentMoment) {
        Log.d(TAG, "🔍 DEBUG_EXTRACT_OWNER: Starting moment owner ID extraction");
        
        try {
            if (currentMoment == null) {
                Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_FAILED: Current moment is null");
                return null;
            }
            
            Log.d(TAG, "📋 Current moment type: " + currentMoment.getClass().getSimpleName());
            
            if (currentMoment instanceof com.example.locket.common.database.entities.MomentEntity) {
                com.example.locket.common.database.entities.MomentEntity moment = 
                    (com.example.locket.common.database.entities.MomentEntity) currentMoment;
                
                Log.d(TAG, "📋 Moment details:");
                Log.d(TAG, "   - ID: " + moment.getId());
                Log.d(TAG, "   - User (username): " + moment.getUser());
                Log.d(TAG, "   - Image URL: " + moment.getImageUrl());
                Log.d(TAG, "   - Caption: " + moment.getCaption());
                
                // Get username from moment and convert to user ID
                String username = moment.getUser();
                if (username == null || username.trim().isEmpty()) {
                    Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_FAILED: Username is null or empty");
                    return null;
                }
                
                Log.d(TAG, "🔄 DEBUG_USER_MAPPING: Looking up user ID for username: " + username);
                
                // Use UserMappingHelper to get proper user ID
                String userId = UserMappingHelper.getUserIdFromUsername(username);
                
                if (userId != null && !userId.equals(username)) {
                    Log.d(TAG, "✅ DEBUG_EXTRACT_OWNER_SUCCESS: Found proper user ID mapping");
                    Log.d(TAG, "   - Username: " + username + " -> User ID: " + userId);
                } else {
                    Log.w(TAG, "⚠️ DEBUG_EXTRACT_OWNER_FALLBACK: Using username as user ID (no mapping found)");
                    Log.w(TAG, "   - This might cause chat room mismatch if username != Firebase user ID");
                    Log.w(TAG, "   - Consider refreshing UserMappingHelper cache");
                }
                
                return userId;
            } else {
                Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_FAILED: Current moment is not a MomentEntity");
                Log.e(TAG, "   - Expected: MomentEntity");
                Log.e(TAG, "   - Actual: " + currentMoment.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_EXCEPTION: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract moment owner ID asynchronously with better user ID resolution
     * @param currentMoment The current moment entity
     * @param callback Callback with the result
     */
    public void extractMomentOwnerIdAsync(Object currentMoment, UserIdCallback callback) {
        Log.d(TAG, "🔍 DEBUG_EXTRACT_OWNER_ASYNC: Starting async moment owner ID extraction");
        
        try {
            if (currentMoment == null) {
                Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_ASYNC_FAILED: Current moment is null");
                if (callback != null) callback.onResult(null);
                return;
            }
            
            if (currentMoment instanceof com.example.locket.common.database.entities.MomentEntity) {
                com.example.locket.common.database.entities.MomentEntity moment = 
                    (com.example.locket.common.database.entities.MomentEntity) currentMoment;
                
                String username = moment.getUser();
                if (username == null || username.trim().isEmpty()) {
                    Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_ASYNC_FAILED: Username is null or empty");
                    if (callback != null) callback.onResult(null);
                    return;
                }
                
                Log.d(TAG, "🔄 DEBUG_USER_MAPPING_ASYNC: Looking up user ID for username: " + username);
                
                // Use async UserMappingHelper to get proper user ID
                UserMappingHelper.getUserIdFromUsernameAsync(username, userId -> {
                    if (userId != null && !userId.equals(username)) {
                        Log.d(TAG, "✅ DEBUG_EXTRACT_OWNER_ASYNC_SUCCESS: Found proper user ID mapping");
                        Log.d(TAG, "   - Username: " + username + " -> User ID: " + userId);
                    } else {
                        Log.w(TAG, "⚠️ DEBUG_EXTRACT_OWNER_ASYNC_FALLBACK: Using username as user ID");
                    }
                    
                    if (callback != null) callback.onResult(userId);
                });
                
            } else {
                Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_ASYNC_FAILED: Current moment is not a MomentEntity");
                if (callback != null) callback.onResult(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ DEBUG_EXTRACT_OWNER_ASYNC_EXCEPTION: " + e.getMessage(), e);
            if (callback != null) callback.onResult(null);
        }
    }

    /**
     * Check if current user is authenticated
     * @return true if user is logged in
     */
    public boolean isUserAuthenticated() {
        boolean isAuthenticated = currentUserId != null && !currentUserId.isEmpty();
        Log.d(TAG, "🔐 User authentication check: " + isAuthenticated + " (ID: " + currentUserId + ")");
        return isAuthenticated;
    }

    /**
     * Get current user ID
     * @return Current user ID or null
     */
    public String getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * Debug method to log current state
     */
    public void logDebugInfo() {
        Log.d(TAG, "🔍 DEBUG_INFO: MessageThreadManager state");
        Log.d(TAG, "   - Current user ID: " + currentUserId);
        Log.d(TAG, "   - Database URL: " + DATABASE_URL);
        Log.d(TAG, "   - User authenticated: " + isUserAuthenticated());
        Log.d(TAG, "   - UserMappingHelper cache initialized: " + UserMappingHelper.isCacheInitialized());
        Log.d(TAG, "   - Cached mappings count: " + UserMappingHelper.getAllMappings().size());
        
        // Log some sample mappings for debugging
        if (!UserMappingHelper.getAllMappings().isEmpty()) {
            Log.d(TAG, "   - Sample mappings:");
            UserMappingHelper.getAllMappings().entrySet().stream()
                .limit(3)
                .forEach(entry -> Log.d(TAG, "     * " + entry.getKey() + " -> " + entry.getValue()));
        }
    }

    /**
     * Callback interface for moment comment message operations
     */
    public interface MomentCommentCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * Callback interface for async user ID lookup
     */
    public interface UserIdCallback {
        void onResult(String userId);
    }
} 