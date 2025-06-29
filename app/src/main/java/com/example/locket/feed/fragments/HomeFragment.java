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

import com.example.locket.common.network.client.LoginApiClient;
import com.example.locket.auth.fragments.LoginOrRegisterFragment;
import com.example.locket.feed.bottomsheets.BottomSheetFriend;
import com.example.locket.feed.bottomsheets.BottomSheetInfo;
import com.example.locket.common.utils.ResponseUtils;
import com.example.locket.common.models.auth.AuthResponse;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.moment.Moment;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.MainActivity;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.models.friendship.FriendsListResponse;

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

public class HomeFragment extends Fragment implements MainActivity.FriendsListUpdateListener {
    private ViewPager2 viewPager;
    private LoginResponse loginResponse;

    private RelativeLayout relative_profile;
    private RelativeLayout relative_send_friend;

    private RoundedImageView img_profile;
    private LinearLayout linear_friends;
    private TextView txt_number_friends;
    private ImageView img_message;
    private MomentApiService momentApiService;
    private FriendshipRepository friendshipRepository;


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
        friendshipRepository = new FriendshipRepository(requireContext());
        initViews(view);
        onCLick();
        conFigViews();
        getDataUser();
        loadFriendsCount(); // Load initial friends count
    }

    private void getDataUser() {
        loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
        checkExpiresToken();
        setupViewPager();
        loadUserProfile(); // Load fresh user profile from API
        
        // Check for pending friend token after user data is loaded
        checkPendingFriendToken();
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
        // Fallback: Load from cached LoginResponse if UserProfile fails
        if (loginResponse != null && loginResponse.getProfilePicture() != null) {
            Glide.with(this).load(loginResponse.getProfilePicture()).into(img_profile);
        }
    }

    /**
     * 🔄 Load fresh user profile from backend API
     */
    private void loadUserProfile() {
        AuthManager.getUserProfile(requireContext(), new AuthManager.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (userProfile != null && userProfile.getUser() != null) {
                    // Save updated profile to SharedPreferences
                    SharedPreferencesUser.saveUserProfile(requireContext(), userProfile);
                    
                    // Update UI with fresh data
                    setDataFromUserProfile(userProfile);
                    
                    Log.d("HomeFragment", "✅ User profile loaded successfully");
                } else {
                    Log.w("HomeFragment", "⚠️ Empty user profile, using cached data");
                    setData(); // Fallback to cached data
                }
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                Log.e("HomeFragment", "❌ Failed to load user profile: " + errorMessage);
                // Fallback to cached LoginResponse data
                setData();
                
                // Handle authentication errors
                if (errorCode == 401) {
                    Log.w("HomeFragment", "🔒 Token expired, redirecting to login");
                    redirectToLogin();
                }
            }
        });
    }

    /**
     * 🎨 Set UI data from UserProfile (fresh from API)
     */
    private void setDataFromUserProfile(UserProfile userProfile) {
        UserProfile.UserData userData = userProfile.getUser();
        
        // Load profile picture
        if (userData.getProfilePicture() != null && !userData.getProfilePicture().isEmpty()) {
            Glide.with(this)
                    .load(userData.getProfilePicture())
                    .placeholder(R.drawable.ic_widget_empty_icon) // Placeholder while loading
                    .error(R.drawable.ic_widget_empty_icon) // Error fallback
                    .into(img_profile);
        } else {
            // Set default avatar if no profile picture
            img_profile.setImageResource(R.drawable.ic_widget_empty_icon);
        }
        
        Log.d("HomeFragment", "🖼️ Profile picture loaded for user: " + userData.getDisplayName());
    }

    /**
     * 🚪 Redirect to login screen when authentication fails
     */
    private void redirectToLogin() {
        SharedPreferencesUser.clearAll(requireContext());
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, new LoginOrRegisterFragment());
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.commit();
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
        // ❌ Backend không có endpoint /auth/refresh - Tạm thời disable
        Log.w("HomeFragment", "Refresh token endpoint not available, skipping refresh");
        
        // Thay vì refresh token, chỉ load data trực tiếp
        getMomentV2(null);
        return;
        
        /*
        // Use real API
        Retrofit retrofit = LoginApiClient.getRefreshTokenClient();
        LoginApiService loginApiService = retrofit.create(LoginApiService.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), createSignInJson("refresh_token", loginResponse.getRefreshToken()));
        Call<ResponseBody> call = loginApiService.REFRESH_TOKEN_RESPONSE_CALL(requestBody);
        
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
                        LoginResponse newLoginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
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
        */
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

    private void getMomentV2(String type) {
        // ❌ Backend không có moments endpoints - Disable để tránh 404
        Log.w("HomeFragment", "Moments endpoint not available, skipping moment fetch");
        
        // Tạm thời hiển thị empty state hoặc mock data
        // moments.clear();
        // momentAdapter.notifyDataSetChanged();
        return;
        
        /* OLD CODE - Endpoint không tồn tại
        Log.d(">>>>>>>>>>>>>", "getMomentV2: ");

        if (loginResponse == null) {
            Log.e("HomeFragment", "LoginResponse is null, cannot fetch moments");
            return;
        }

        String idToken = loginResponse.getIdToken();
        if (idToken == null || idToken.isEmpty()) {
            Log.e("HomeFragment", "ID token is null or empty, cannot fetch moments");
            return;
        }

        // Use real API
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), createMomentJson(idToken, type));
        Call<ResponseBody> getMomentCall = momentApiService.GET_MOMENT_RESPONSE_CALL(requestBody);

        getMomentCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String contentEncoding = response.headers().get("Content-Encoding");
                        String responseBody = ResponseUtils.getResponseBody(response.body().byteStream(), contentEncoding);

                        Log.d(">>>>>>>>>>>>>", "getMomentV2: " + responseBody);

                        Gson gson = new Gson();
                        Result result = gson.fromJson(responseBody, Result.class);

                        moments.clear();
                        moments.addAll(result.getData());

                        momentAdapter.notifyDataSetChanged();

                    } catch (IOException e) {
                        Log.e("Moment", "Error reading response body", e);
                    }
                } else {
                    Log.e("Moment", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Moment", "Error: " + t.getMessage());
            }
        });
        */
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

    // ==================== FRIENDS LIST MANAGEMENT ====================

    /**
     * 👥 Load friends count from API
     */
    private void loadFriendsCount() {
        friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse friendsListResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        int friendsCount = 0;
                        if (friendsListResponse != null && friendsListResponse.getData() != null) {
                            friendsCount = friendsListResponse.getData().size();
                        }
                        updateFriendsCountUI(friendsCount);
                        Log.d("HomeFragment", "✅ Friends count loaded: " + friendsCount);
                    });
                }
            }

            @Override
            public void onError(String message, int code) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e("HomeFragment", "❌ Failed to load friends count: " + message);
                        updateFriendsCountUI(0); // Show 0 as fallback
                    });
                }
            }

            @Override
            public void onLoading(boolean isLoading) {
                // Optional: Show loading indicator
            }
        });
    }

    /**
     * 🎨 Update friends count in UI
     */
    @SuppressLint("SetTextI18n")
    private void updateFriendsCountUI(int count) {
        if (txt_number_friends != null) {
            String friendsText = count == 1 ? count + " Bạn bè" : count + " Bạn bè";
            txt_number_friends.setText(friendsText);
        }
    }

    /**
     * 🔄 Implementation of FriendsListUpdateListener
     * Called when friends list is updated (e.g., after accepting friend invite)
     */
    @Override
    public void onFriendsListUpdated() {
        Log.d("HomeFragment", "🔄 Friends list updated, refreshing count...");
        loadFriendsCount();
    }

    /**
     * 🔍 Check for pending friend token and process it
     */
    private void checkPendingFriendToken() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).checkPendingFriendToken();
        }
    }
}

