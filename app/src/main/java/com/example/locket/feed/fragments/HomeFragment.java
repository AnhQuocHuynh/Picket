package com.example.locket.feed.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.example.locket.common.models.moment.Data;
import com.makeramen.roundedimageview.RoundedImageView;
import com.example.locket.R;
import com.example.locket.feed.adapters.HomeViewPager2Adapter;
import com.example.locket.common.network.LoginApiService;
import com.example.locket.common.network.MomentApiService;
import com.example.locket.common.network.MockApiServer;
import com.example.locket.common.network.MockLoginService;
import com.example.locket.common.network.client.LoginApiClient;
import com.example.locket.auth.fragments.LoginOrRegisterFragment;
import com.example.locket.feed.bottomsheets.BottomSheetFriend;
import com.example.locket.feed.bottomsheets.BottomSheetInfo;
import com.example.locket.common.utils.ResponseUtils;
import com.example.locket.common.models.auth.AuthResponse;
import com.example.locket.common.models.auth.LoginRespone;
import com.example.locket.common.models.moment.Moment;
import com.example.locket.common.utils.SharedPreferencesUser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {
    private ViewPager2 viewPager;
    private LoginRespone loginResponse;

    private RelativeLayout relative_profile;
    private RelativeLayout relative_send_friend;

    private RoundedImageView img_profile;
    private LinearLayout linear_friends;
    private TextView txt_number_friends;
    private ImageView img_message;
    private MomentApiService momentApiService;


    public BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    return;
                }
                int position = bundle.getInt("position");
                viewPager.setUserInputEnabled(position == 0);
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        momentApiService = LoginApiClient.getCheckEmailClient().create(MomentApiService.class);
        initViews(view);
        onCLick();
        conFigViews();
        getDataUser();
    }

    private void getDataUser() {
        loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
        checkExpiresToken();
        setupViewPager();
        setData();
    }

    private void initViews(View view) {
        relative_profile = view.findViewById(R.id.relative_profile);
        relative_send_friend = view.findViewById(R.id.relative_send_friend);

        img_profile = view.findViewById(R.id.img_profile);
        linear_friends = view.findViewById(R.id.linear_friends);
        txt_number_friends = view.findViewById(R.id.txt_number_friends);
        img_message = view.findViewById(R.id.img_message);

        viewPager = view.findViewById(R.id.viewPager);
    }

    private void conFigViews() {

    }

    private void onCLick() {
        linear_friends.setOnClickListener(view -> {
            BottomSheetFriend bottomSheetFriend = new BottomSheetFriend(requireContext(), getActivity());
            bottomSheetFriend.show(getParentFragmentManager(), bottomSheetFriend.getTag());
        });
        img_profile.setOnClickListener(view -> {
            BottomSheetInfo bottomSheetInfo = new BottomSheetInfo(requireContext(), getActivity());
            bottomSheetInfo.show(getParentFragmentManager(), bottomSheetInfo.getTag());
        });
    }

    private void setData() {
        Glide.with(this).load(loginResponse.getProfilePicture()).into(img_profile);
    }

    private void setupViewPager() {
        HomeViewPager2Adapter adapter = new HomeViewPager2Adapter(requireActivity());
        viewPager.setAdapter(adapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
    }

    private void checkExpiresToken() {
//        AccountInfo accountInfo = SharedPreferencesUser.getAccountInfo(requireContext());
//
//        // Lấy thời gian làm mới cuối cùng (milliseconds)
//        long lastLoginTime = convertTime(accountInfo.getUsers().get(0).getLastRefreshAt());
//
//        // Thời hạn hiệu lực token (3500 giây = 58 phút 20 giây)
//        long expiryDuration = 3500 * 1000L;
//
//        // Thời gian hết hạn của token
//        long expiryTime = lastLoginTime + expiryDuration;
//
//        // Lấy thời gian hiện tại
//        long currentTime = System.currentTimeMillis();
//
//        // Kiểm tra nếu token hết hạn
//        if (currentTime >= expiryTime) {
//            refreshToken();
//        }
        refreshToken();
    }


    private long convertTime(String lastRefreshAt) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8+ (API 26+)
                return Instant.parse(lastRefreshAt).toEpochMilli();
            } else {
                // Android 7- (API < 26)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(lastRefreshAt);
                return (date != null) ? date.getTime() : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Trả về 0 nếu lỗi xảy ra
        }
    }


    private String createSignInJson(String grantType, String refreshToken) {
        return String.format(
                "{\"grantType\":\"%s\",\"refreshToken\":\"%s\"}",
                grantType, refreshToken
        );
    }

    private void refreshToken() {
        Call<ResponseBody> call;
        
        // Use mock service if in mock mode and token contains "mock"
        if (MockLoginService.isMockMode() && loginResponse.getRefreshToken() != null && loginResponse.getRefreshToken().contains("mock")) {
            Log.d("HomeFragment", "Using mock refresh token");
            call = MockLoginService.mockRefreshToken(loginResponse.getRefreshToken());
        } else {
            // Use real API
            Retrofit retrofit = LoginApiClient.getRefreshTokenClient();
            LoginApiService loginApiService = retrofit.create(LoginApiService.class);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), createSignInJson("refresh_token", loginResponse.getRefreshToken()));
            call = loginApiService.REFRESH_TOKEN_RESPONSE_CALL(requestBody);
        }
        
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String contentEncoding = response.headers().get("Content-Encoding");
                        String responseBody = ResponseUtils.getResponseBody(response.body().byteStream(), contentEncoding);

                        Gson gson = new Gson();
                        AuthResponse authResponse = gson.fromJson(responseBody, AuthResponse.class);


                        //save user
                        LoginRespone newLoginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
                        Log.d(">>>>>>>>>>>>>>>", "old Token: " + newLoginResponse.getIdToken());
                        Log.d(">>>>>>>>>>>>>>>", "new Token: " + authResponse.getIdToken());

                        newLoginResponse.setIdToken(authResponse.getIdToken());
                        newLoginResponse.setRefreshToken(authResponse.getRefreshToken());
                        SharedPreferencesUser.saveLoginResponse(requireContext(), newLoginResponse);
                        loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());


                        getMomentV2(null);
                    } catch (IOException e) {
                        Log.e("Auth", "Error reading response body", e);
                    }
                } else {
                    showAlertDialog("Phiên đăng nhập hết hạn", "Vui lòng đăng nhập lại để tiếp tục sử dụng ứng dụng.");
                    SharedPreferencesUser.clearAll(requireContext());
                    releaseFragment();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("login", "Error: " + t.getMessage());
            }

        });
    }

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

    private void getMomentV2(List<String> excludedUsers) {
        if (excludedUsers == null) {
            excludedUsers = new ArrayList<>();
        }

        String token = "Bearer " + loginResponse.getIdToken();
        Log.d("HomeFragment", "getMomentV2: " + token);
        
        Call<ResponseBody> ResponseBodyCall;
        
        // Use mock service if in mock mode and token contains "mock"
        if (MockLoginService.isMockMode() && token.contains("mock")) {
            Log.d("HomeFragment", "Using mock moments API");
            ResponseBodyCall = MockApiServer.getMockMomentsResponse();
        } else {
            // Use real API
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), createGetMomentV2ExcludedUsersJson(excludedUsers));
            ResponseBodyCall = momentApiService.GET_MOMENT_V2(token, requestBody);
        }
        
        List<String> finalExcludedUsers = excludedUsers;
        ResponseBodyCall.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        Moment moment = gson.fromJson(responseBody, Moment.class);
                        
                        // Handle mock mode differently - don't do recursive calls
                        if (MockLoginService.isMockMode() && loginResponse.getIdToken().contains("mock")) {
                            Log.d("HomeFragment", "Processing mock moments data");
                            if (moment.getResult() != null && moment.getResult().getData() != null) {
                                List<String> mockFriends = new ArrayList<>();
                                for (Data data : moment.getResult().getData()) {
                                    if (!mockFriends.contains(data.getUser())) {
                                        mockFriends.add(data.getUser());
                                    }
                                }
                                SharedPreferencesUser.saveUserFriends(requireContext(), mockFriends);
                                txt_number_friends.setText(mockFriends.size() + " Bạn bè");
                            } else {
                                txt_number_friends.setText("0 Bạn bè");
                            }
                        } else {
                            // Real API logic
                            if (!moment.getResult().getData().isEmpty()) {
                                finalExcludedUsers.add(moment.getResult().getData().get(0).getUser());
                                getMomentV2(finalExcludedUsers); // Gọi đệ quy với danh sách đã cập nhật
                            } else {
                                SharedPreferencesUser.saveUserFriends(requireContext(), finalExcludedUsers);
                                txt_number_friends.setText(finalExcludedUsers.size() + " Bạn bè");
                            }
                        }
                    } catch (IOException e) {
                        Log.e("HomeFragment", "Error reading response body", e);
                    }
                } else {
                    Log.e("HomeFragment", "Failed response from getMomentV2");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.e("Response Error", "Unsuccessful response: " + throwable.getMessage());
            }
        });
    }


    private void showAlertDialog(String title, String content) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void releaseFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, new LoginOrRegisterFragment());
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(createBroadcastReceiver(), new IntentFilter("send_position_swipe_viewpage2"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(createBroadcastReceiver());
    }
}

