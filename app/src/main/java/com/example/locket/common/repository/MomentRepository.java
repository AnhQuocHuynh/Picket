package com.example.locket.common.repository;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.google.gson.Gson;
import com.example.locket.common.network.MomentApiService;

import com.example.locket.common.network.client.LoginApiClient;
import com.example.locket.common.database.AppDatabase;
import com.example.locket.common.database.dao.MomentDao;
import com.example.locket.common.database.entities.MomentEntity;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.moment.Data;
import com.example.locket.common.models.moment.Moment;
import com.example.locket.common.utils.SharedPreferencesUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomentRepository {
    private final LiveData<List<MomentEntity>> allMoments;
    private final MomentApiService momentApiService;
    private final Context context;
    private final LoginResponse loginResponse; // Giả sử đây là model chứa token (idToken, vv)

    public MomentRepository(Application application) {
        this.context = application;
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "moment_database")
                .fallbackToDestructiveMigration()
                .build();
        MomentDao momentDao = db.momentDao();
        allMoments = momentDao.getAllMoments();
        momentApiService = LoginApiClient.getCheckEmailClient().create(MomentApiService.class);
        loginResponse = SharedPreferencesUser.getLoginResponse(application);
    }

    // --- Phương thức tạo JSON cho API ---
    @SuppressLint("DefaultLocale")
    private String createGetMomentV2ExcludedUsersJson(List<String> excludedUsers) {
        String excludedUsersJson = (excludedUsers == null || excludedUsers.isEmpty())
                ? "[]"
                : new Gson().toJson(excludedUsers);

        return String.format(
                Locale.getDefault(),
                "{\"data\":{" +
                        "\"excluded_users\":%s," +
                        "\"last_fetch\":%d," +
                        "\"should_count_missed_moments\":%b" +
                        "}}",
                excludedUsersJson,
                1,
                true
        );
    }

    public void refreshDataFromServer() {
        // ❌ Backend không có moments endpoints - Disable để tránh 404
        Log.w("MomentRepository", "Moments endpoint not available, skipping server refresh");
        return;
        
        /* OLD CODE - Endpoint không tồn tại
        LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(context);
        if (loginResponse == null) {
            Log.e("MomentRepository", "No login response available");
            return;
        }
        
        String idToken = loginResponse.getIdToken();
        if (idToken == null || idToken.isEmpty()) {
            Log.e("MomentRepository", "No ID token available");
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), createMomentJson(idToken));
        Call<ResponseBody> call = momentApiService.GET_MOMENT_RESPONSE_CALL(requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = ResponseUtils.getResponseBody(response.body().byteStream(), "");
                        Gson gson = new Gson();
                        Result result = gson.fromJson(responseBody, Result.class);

                        // Save to database
                        List<MomentEntity> momentEntities = new ArrayList<>();
                        for (Data momentData : result.getData()) {
                            MomentEntity entity = new MomentEntity();
                            entity.id = momentData.getId();
                            entity.user = momentData.getUser();
                            entity.imageUrl = momentData.getImageUrl();
                            entity.caption = momentData.getCaption();
                            entity.timestamp = System.currentTimeMillis();
                            momentEntities.add(entity);
                        }

                        new Thread(() -> {
                            momentDao.deleteAll();
                            momentDao.insertAll(momentEntities);
                        }).start();

                    } catch (IOException e) {
                        Log.e("MomentRepository", "Error reading response body", e);
                    }
                } else {
                    Log.e("MomentRepository", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("MomentRepository", "Network error: " + t.getMessage());
            }
        });
        */
    }

    public LiveData<List<MomentEntity>> getAllMoments() {
        return allMoments;
    }
}

