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
import com.example.locket.common.database.dao.FriendDao;
import com.example.locket.common.models.friend.Friend;
import com.example.locket.common.models.friend.UserData;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.database.entities.FriendEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRepository {
    private final LiveData<List<FriendEntity>> allFriends;
    private final MomentApiService momentApiService;
    private final Context context;
    private final LoginResponse loginResponse; // Giả sử đây là model chứa token (idToken, vv)

    public FriendRepository(Application application) {
        this.context = application;
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "friend_database")
                .fallbackToDestructiveMigration()
                .build();
        FriendDao friendDao = db.friendDao();
        allFriends = friendDao.getAllFriends();
        momentApiService = LoginApiClient.getCheckEmailClient().create(MomentApiService.class);
        loginResponse = SharedPreferencesUser.getLoginResponse(application);
    }

    // --- Phương thức tạo JSON cho API ---
    @SuppressLint("DefaultLocale")
    private String createGetMomentV2ExcludedUsersJson(List<String> excludedUsers) {
        String excludedUsersJson = (excludedUsers == null || excludedUsers.isEmpty()) ? "[]" : new Gson().toJson(excludedUsers);

        return String.format(
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

    public void refreshDataFromServer(List<String> excludedUsers) {
        // ❌ Backend không có friends/moments endpoints - Disable để tránh 404
        Log.w("FriendRepository", "Friends/moments endpoint not available, skipping server refresh");
        return;
        
        /* OLD CODE - Endpoints không tồn tại
        if (excludedUsers == null) {
            excludedUsers = new ArrayList<>();
        }

        // Kiểm tra xem user đã login chưa
        if (loginResponse == null) {
            Log.w("FriendRepository", "User not logged in, skipping data refresh");
            return;
        }

        String token = "Bearer " + loginResponse.getIdToken();
        
        // Use real API
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=UTF-8"),
                createGetMomentV2ExcludedUsersJson(excludedUsers)
        );
        Call<ResponseBody> responseBodyCall = momentApiService.GET_MOMENT_V2(token, requestBody);
        
        // Lưu ý: Nếu bạn không cần gọi đệ quy (pagination) thì có thể bỏ logic cập nhật excludedUsers và gọi lại refreshDataFromServer
        List<String> finalExcludedUsers = excludedUsers;

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        // Chuyển đổi JSON thành đối tượng Friend
                        Friend friendResponse = gson.fromJson(responseBody, Friend.class);

                        if (friendResponse.getResult().getData() != null) {
                            // Lấy danh sách dữ liệu (mảng data) từ server
                            List<UserData> dataList = (List<UserData>) friendResponse.getResult().getData();
                            List<FriendEntity> entityList = new ArrayList<>();

                            // Map từng UserData sang FriendEntity
                            for (UserData userData : dataList) {
                                FriendEntity entity = new FriendEntity(
                                        userData.getUid(),
                                        userData.getFirst_name(),
                                        userData.getLast_name(),
                                        userData.getBadge(),
                                        userData.getProfile_picture_url(),
                                        userData.isTemp(),
                                        userData.getUsername()
                                );
                                entityList.add(entity);
                            }

                            // Lưu dữ liệu vào Room trên background thread
                            new Thread(() -> {
                                // Lấy instance của AppDatabase; nếu có singleton, dùng nó thay vì tạo mới
                                AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "friend_database")
                                        .fallbackToDestructiveMigration()
                                        .build();
                                db.friendDao().insertAll(entityList);
                            }).start();

                            // Real API logic - recursive calls
                            finalExcludedUsers.add(dataList.get(0).getUsername());
                            Log.d("FriendRepository", "onResponse: "+dataList.get(0).getUsername());
                            // Gọi lại API đệ quy để load thêm dữ liệu nếu cần
                            refreshDataFromServer(finalExcludedUsers);
                        }
                    } catch (IOException e) {
                        Log.e("FriendRepository", "Error reading response body", e);
                    }
                } else {
                    Log.e("FriendRepository", "Response unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("FriendRepository", "Error fetching data", t);
            }
        });
        */
    }

    // Phương thức overload nếu không cần truyền excludedUsers (bắt đầu mới)
    public void refreshDataFromServer() {
        // ❌ Backend không có friends endpoints - Disable để tránh 404
        Log.w("FriendRepository", "Friends endpoint not available, skipping server refresh");
        return;
        
        /* OLD CODE - Endpoint không tồn tại
        LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(context);
        if (loginResponse == null) {
            Log.e("FriendRepository", "No login response available");
            return;
        }
        
        String idToken = loginResponse.getIdToken();
        if (idToken == null || idToken.isEmpty()) {
            Log.e("FriendRepository", "No ID token available");
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), createFriendJson(idToken));
        Call<ResponseBody> call = friendApiService.GET_FRIEND_RESPONSE_CALL(requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = ResponseUtils.getResponseBody(response.body().byteStream(), "");
                        Gson gson = new Gson();
                        // Parse friend data and save to database
                        // Implementation depends on your Friend model structure
                        
                    } catch (IOException e) {
                        Log.e("FriendRepository", "Error reading response body", e);
                    }
                } else {
                    Log.e("FriendRepository", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("FriendRepository", "Network error: " + t.getMessage());
            }
        });
        */
    }

    public LiveData<List<FriendEntity>> getAllFriends() {
        return allFriends;
    }
}

