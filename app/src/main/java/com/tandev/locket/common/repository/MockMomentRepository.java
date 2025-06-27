package com.tandev.locket.common.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.tandev.locket.common.database.AppDatabase;
import com.tandev.locket.common.database.dao.MomentDao;
import com.tandev.locket.common.database.entities.MomentEntity;
import com.tandev.locket.common.network.MockApiServer;
import com.tandev.locket.common.utils.MockDataService;

import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MockMomentRepository {
    private static final String TAG = "MockMomentRepository";
    private final MutableLiveData<List<MomentEntity>> allMoments;
    private final Context context;
    private final MomentDao momentDao;

    public MockMomentRepository(Application application) {
        this.context = application;
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "moment_database")
                .fallbackToDestructiveMigration()
                .build();
        momentDao = db.momentDao();
        allMoments = new MutableLiveData<>();
        
        // Load mock data initially
        loadMockData();
    }

    private void loadMockData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MomentEntity> mockMoments = MockDataService.getMockMoments();
            
            // Insert into database
            momentDao.insertAll(mockMoments);
            
            // Update LiveData
            allMoments.postValue(mockMoments);
            
            Log.d(TAG, "Loaded " + mockMoments.size() + " mock moments");
        });
    }

    public void refreshDataFromServer(List<String> excludedUsers) {
        Log.d(TAG, "Refreshing data from mock server...");
        
        Call<ResponseBody> call = MockApiServer.getMockMomentsResponse();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Mock API response successful");
                    // Simulate data refresh by reloading mock data
                    loadMockData();
                } else {
                    Log.e(TAG, "Mock API response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Mock API call failed", t);
                // Still load mock data on failure
                loadMockData();
            }
        });
    }

    public void refreshDataFromServer() {
        refreshDataFromServer(null);
    }

    public LiveData<List<MomentEntity>> getAllMoments() {
        return allMoments;
    }

    // Add new moment (for testing post functionality)
    public void addMockMoment(String caption, String thumbnailUrl) {
        Executors.newSingleThreadExecutor().execute(() -> {
            long currentTime = System.currentTimeMillis() / 1000;
            MomentEntity newMoment = new MomentEntity(
                "moment_" + currentTime,
                "mock_user_12345",
                thumbnailUrl,
                currentTime,
                caption,
                "md5_" + currentTime,
                null
            );
            
            momentDao.insert(newMoment);
            
            // Refresh LiveData
            List<MomentEntity> currentMoments = allMoments.getValue();
            if (currentMoments != null) {
                currentMoments.add(0, newMoment); // Add to beginning
                allMoments.postValue(currentMoments);
            }
            
            Log.d(TAG, "Added new mock moment: " + caption);
        });
    }
} 