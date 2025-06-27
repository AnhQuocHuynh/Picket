package com.tandev.locket.common.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.tandev.locket.common.database.AppDatabase;
import com.tandev.locket.common.database.dao.FriendDao;
import com.tandev.locket.common.database.entities.FriendEntity;
import com.tandev.locket.common.network.MockApiServer;
import com.tandev.locket.common.utils.MockDataService;

import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MockFriendRepository {
    private static final String TAG = "MockFriendRepository";
    private final MutableLiveData<List<FriendEntity>> allFriends;
    private final Context context;
    private final FriendDao friendDao;

    public MockFriendRepository(Application application) {
        this.context = application;
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "friend_database")
                .fallbackToDestructiveMigration()
                .build();
        friendDao = db.friendDao();
        allFriends = new MutableLiveData<>();
        
        // Load mock data initially
        loadMockData();
    }

    private void loadMockData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FriendEntity> mockFriends = MockDataService.getMockFriends();
            
            // Insert into database
            friendDao.insertAll(mockFriends);
            
            // Update LiveData
            allFriends.postValue(mockFriends);
            
            Log.d(TAG, "Loaded " + mockFriends.size() + " mock friends");
        });
    }

    public void refreshDataFromServer() {
        Log.d(TAG, "Refreshing friends data from mock server...");
        
        Call<ResponseBody> call = MockApiServer.getMockFriendsResponse();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Mock Friends API response successful");
                    // Simulate data refresh by reloading mock data
                    loadMockData();
                } else {
                    Log.e(TAG, "Mock Friends API response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Mock Friends API call failed", t);
                // Still load mock data on failure
                loadMockData();
            }
        });
    }

    public LiveData<List<FriendEntity>> getAllFriends() {
        return allFriends;
    }

    // Add new friend (for testing add friend functionality)
    public void addMockFriend(String firstName, String lastName, String username) {
        Executors.newSingleThreadExecutor().execute(() -> {
            long currentTime = System.currentTimeMillis();
            FriendEntity newFriend = new FriendEntity(
                "friend_" + currentTime,
                firstName,
                lastName,
                "🆕",
                "https://i.pravatar.cc/300?img=" + (currentTime % 70 + 1),
                false,
                username
            );
            
            friendDao.insert(newFriend);
            
            // Refresh LiveData
            List<FriendEntity> currentFriends = allFriends.getValue();
            if (currentFriends != null) {
                currentFriends.add(newFriend);
                allFriends.postValue(currentFriends);
            }
            
            Log.d(TAG, "Added new mock friend: " + firstName + " " + lastName);
        });
    }

    // Search friends by name
    public void searchFriends(String query, SearchCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FriendEntity> allFriendsList = allFriends.getValue();
            if (allFriendsList != null) {
                List<FriendEntity> filteredList = allFriendsList.stream()
                    .filter(friend -> 
                        friend.getFirst_name().toLowerCase().contains(query.toLowerCase()) ||
                        friend.getLast_name().toLowerCase().contains(query.toLowerCase()) ||
                        friend.getUsername().toLowerCase().contains(query.toLowerCase())
                    )
                    .collect(java.util.stream.Collectors.toList());
                
                callback.onSearchResult(filteredList);
            }
        });
    }

    public interface SearchCallback {
        void onSearchResult(List<FriendEntity> friends);
    }
} 