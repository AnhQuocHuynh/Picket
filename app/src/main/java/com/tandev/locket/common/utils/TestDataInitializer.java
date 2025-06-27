package com.tandev.locket.common.utils;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.tandev.locket.common.database.AppDatabase;
import com.tandev.locket.common.database.dao.FriendDao;
import com.tandev.locket.common.database.dao.MomentDao;
import com.tandev.locket.common.database.entities.FriendEntity;
import com.tandev.locket.common.database.entities.MomentEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class TestDataInitializer {
    
    private static final String TAG = "TestDataInitializer";
    private static final String PREF_NAME = "test_data_prefs";
    private static final String KEY_DATA_INITIALIZED = "data_initialized";
    
    /**
     * Initialize all mock data for testing
     * Call this in MainActivity onCreate or Application class
     */
    public static void initializeTestData(Context context) {
        // Check if data already initialized
        if (isDataAlreadyInitialized(context)) {
            Log.d(TAG, "Test data already initialized, skipping...");
            return;
        }
        
        Log.d(TAG, "Initializing test data...");
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Initialize login data
                initializeLoginData(context);
                
                // Initialize database data
                initializeDatabaseData(context);
                
                // Mark as initialized
                markDataAsInitialized(context);
                
                Log.d(TAG, "Test data initialization completed successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error initializing test data", e);
            }
        });
    }
    
    /**
     * Force reinitialize data (useful for testing)
     */
    public static void forceReinitializeTestData(Context context) {
        markDataAsNotInitialized(context);
        initializeTestData(context);
    }
    
    private static void initializeLoginData(Context context) {
        // Save mock login response to SharedPreferences
        MockDataService.populateMockData(context);
        Log.d(TAG, "Login data initialized");
    }
    
    private static void initializeDatabaseData(Context context) {
        // Initialize Room database with mock data
        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "moment_database")
                .fallbackToDestructiveMigration()
                .build();
        
        MomentDao momentDao = db.momentDao();
        FriendDao friendDao = db.friendDao();
        
        // Clear existing data
        momentDao.deleteAll();
        friendDao.deleteAll();
        
        // Insert mock moments
        List<MomentEntity> mockMoments = MockDataService.getMockMoments();
        momentDao.insertAll(mockMoments);
        Log.d(TAG, "Inserted " + mockMoments.size() + " mock moments");
        
        // Insert mock friends
        List<FriendEntity> mockFriends = MockDataService.getMockFriends();
        friendDao.insertAll(mockFriends);
        Log.d(TAG, "Inserted " + mockFriends.size() + " mock friends");
        
        db.close();
    }
    
    private static boolean isDataAlreadyInitialized(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DATA_INITIALIZED, false);
    }
    
    private static void markDataAsInitialized(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DATA_INITIALIZED, true)
                .apply();
    }
    
    private static void markDataAsNotInitialized(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DATA_INITIALIZED, false)
                .apply();
    }
    
    /**
     * Clear all test data
     */
    public static void clearTestData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Clear database
                AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "moment_database")
                        .fallbackToDestructiveMigration()
                        .build();
                
                db.momentDao().deleteAll();
                db.friendDao().deleteAll();
                db.close();
                
                // Clear SharedPreferences
                SharedPreferencesUser.clearLoginResponse(context);
                markDataAsNotInitialized(context);
                
                Log.d(TAG, "Test data cleared successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error clearing test data", e);
            }
        });
    }
    
    /**
     * Add some additional test scenarios
     */
    public static void addAdditionalTestData(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "moment_database")
                        .fallbackToDestructiveMigration()
                        .build();
                
                long currentTime = System.currentTimeMillis() / 1000;
                
                // Add some recent moments
                MomentEntity recentMoment1 = new MomentEntity(
                    "recent_001",
                    "friend_001",
                    "https://picsum.photos/400/600?random=100",
                    currentTime - 300, // 5 minutes ago
                    "Just now! 📸",
                    "md5_recent_001",
                    null
                );
                
                MomentEntity recentMoment2 = new MomentEntity(
                    "recent_002",
                    "friend_002",
                    "https://picsum.photos/400/600?random=101",
                    currentTime - 600, // 10 minutes ago
                    "Testing the app! 🧪",
                    "md5_recent_002",
                    null
                );
                
                db.momentDao().insert(recentMoment1);
                db.momentDao().insert(recentMoment2);
                
                // Add a new friend
                FriendEntity newFriend = new FriendEntity(
                    "test_friend_001",
                    "Test",
                    "Friend",
                    "🧪",
                    "https://i.pravatar.cc/300?img=99",
                    false,
                    "testfriend"
                );
                
                db.friendDao().insert(newFriend);
                
                db.close();
                
                Log.d(TAG, "Additional test data added");
                
            } catch (Exception e) {
                Log.e(TAG, "Error adding additional test data", e);
            }
        });
    }
} 