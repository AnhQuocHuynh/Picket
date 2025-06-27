package com.tandev.locket.common.repository;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.google.gson.Gson;
import com.tandev.locket.common.network.MomentApiService;
import com.tandev.locket.common.network.MockApiServer;
import com.tandev.locket.common.network.MockLoginService;
import com.tandev.locket.common.network.client.LoginApiClient;
import com.tandev.locket.common.database.AppDatabase;
import com.tandev.locket.common.database.dao.MomentDao;
import com.tandev.locket.common.database.entities.MomentEntity;
import com.tandev.locket.common.models.auth.LoginRespone;
import com.tandev.locket.common.models.moment.Data;
import com.tandev.locket.common.models.moment.Moment;
import com.tandev.locket.common.utils.SharedPreferencesUser;

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
    private final LoginRespone loginResponse; // Giả sử đây là model chứa token (idToken, vv)

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

    public void refreshDataFromServer(List<String> excludedUsers) {
        if (excludedUsers == null) {
            excludedUsers = new ArrayList<>();
        }

        String token = "Bearer " + loginResponse.getIdToken();
        
        Call<ResponseBody> responseBodyCall;
        
        // Use mock service if in mock mode and token contains "mock"
        if (MockLoginService.isMockMode() && token.contains("mock")) {
            Log.d("MomentRepository", "Using mock moments API");
            responseBodyCall = MockApiServer.getMockMomentsResponse();
        } else {
            // Use real API
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=UTF-8"),
                    createGetMomentV2ExcludedUsersJson(excludedUsers)
            );
            responseBodyCall = momentApiService.GET_MOMENT_V2(token, requestBody);
        }
        
        // Lưu ý: Nếu bạn không cần gọi đệ quy (pagination) thì có thể bỏ logic cập nhật excludedUsers và gọi lại refreshDataFromServer
        List<String> finalExcludedUsers = excludedUsers;

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        // Chuyển đổi JSON thành đối tượng Moment
                        Moment moment = gson.fromJson(responseBody, Moment.class);

                        if (moment.getResult() != null && moment.getResult().getData() != null && !moment.getResult().getData().isEmpty()) {
                            // Lấy danh sách dữ liệu (mảng data) từ server
                            List<Data> dataList = moment.getResult().getData();
                            List<MomentEntity> entityList = new ArrayList<>();

                            // Map từng Data sang MomentEntity
                            for (Data data : dataList) {
                                long dateSeconds = data.getDate().get_seconds(); // Lấy _seconds từ đối tượng Date
                                MomentEntity entity = new MomentEntity(
                                        data.getCanonical_uid(),
                                        data.getUser(),
                                        data.getThumbnail_url(),
                                        dateSeconds,
                                        data.getCaption(),
                                        data.getMd5(),
                                        data.getOverlays()    // List<Overlay> – sẽ được chuyển đổi qua Converter
                                );
                                entityList.add(entity);
                            }

                            // Lưu dữ liệu vào Room trên background thread
                            new Thread(() -> {
                                // Lấy instance của AppDatabase; nếu có singleton, dùng nó thay vì tạo mới
                                AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "moment_database")
                                        .fallbackToDestructiveMigration()
                                        .build();
                                db.momentDao().insertAll(entityList);
                            }).start();

                            // Cập nhật danh sách excludedUsers cho lần gọi tiếp theo (nếu dùng phân trang)
                            if (!dataList.isEmpty()) {
                                finalExcludedUsers.add(dataList.get(0).getUser());
                                // Gọi lại API đệ quy để load thêm dữ liệu nếu cần
                                refreshDataFromServer(finalExcludedUsers);
                            }
                        }
                    } catch (IOException e) {
                        Log.e("MomentRepository", "Error reading response body", e);
                    }
                } else {
                    Log.e("MomentRepository", "Response unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("MomentRepository", "Error fetching data", t);
            }
        });
    }

    // Phương thức overload nếu không cần truyền excludedUsers (bắt đầu mới)
    public void refreshDataFromServer() {
        refreshDataFromServer(new ArrayList<>());
    }

    public LiveData<List<MomentEntity>> getAllMoments() {
        return allMoments;
    }
}

