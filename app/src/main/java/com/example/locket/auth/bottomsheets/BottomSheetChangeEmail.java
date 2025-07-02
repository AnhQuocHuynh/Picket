package com.example.locket.auth.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.locket.R;
import com.example.locket.auth.fragments.ChangeEmailFragment;
import com.example.locket.auth.fragments.CheckPassFragment;
import com.example.locket.common.models.auth.LoginRequest;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.network.AuthApiService;
import com.example.locket.common.network.LoginApiService;
import com.example.locket.common.network.UserApiService;
import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.network.client.LoginApiClient;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.utils.WidgetUpdateHelper;
import com.example.locket.feed.bottomsheets.BottomSheetInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BottomSheetChangeEmail extends BottomSheetDialogFragment implements CheckPassFragment.OnPasswordChangedListener, ChangeEmailFragment.OnEmailChangedListener {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;

    private UserApiService userApiService;

    private LinearLayout tool_bar, linear_continue;
    private ImageView img_back;
    private TextView txt_continue;

    private Retrofit retrofit;
    private LoginApiService loginApiService;

    public BottomSheetChangeEmail(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme; // Áp dụng theme tùy chỉnh
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.item_bottom_sheet_change_email, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Initialize fragment and set listener
        CheckPassFragment checkPassFragment = new CheckPassFragment();
        checkPassFragment.setOnPasswordChangedListener(this); // Set listener here

        ChangeEmailFragment changeEmailFragment = new ChangeEmailFragment();
        changeEmailFragment.setOnEmailChangedListener(this); // Set listener here


        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, checkPassFragment);
        transaction.commit();

        retrofit = LoginApiClient.getLoginClient();
        loginApiService = retrofit.create(LoginApiService.class);

        initViews(bottomSheetDialog);
        conFigViews();
        onClick();

        return bottomSheetDialog;
    }

    private void initViews(BottomSheetDialog bottomSheetDialog) {
        tool_bar = bottomSheetDialog.findViewById(R.id.tool_bar);
        img_back = bottomSheetDialog.findViewById(R.id.img_back);
        linear_continue = bottomSheetDialog.findViewById(R.id.linear_continue);
        txt_continue = bottomSheetDialog.findViewById(R.id.txt_continue);
    }

    private void conFigViews() {

    }

    private void onClick() {
        linear_continue.setOnClickListener(view -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof ChangeEmailFragment) {
                ChangeEmailFragment changeEmailFragment = (ChangeEmailFragment) getChildFragmentManager().findFragmentById(R.id.frame_layout);
                if (changeEmailFragment != null) {
                    String email = changeEmailFragment.getPassword(); // Lấy email từ Fragment2
                    changeEmail(email);
                }
            } else {
                CheckPassFragment checkPassFragment = (CheckPassFragment) getChildFragmentManager().findFragmentById(R.id.frame_layout);
                if (checkPassFragment != null) {
                    String password = checkPassFragment.getPassword(); // Lấy pass từ Fragment1
                    login(SharedPreferencesUser.getLoginRequest(context).getEmail(), password);
                }
            }
        });
        img_back.setOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
                tool_bar.setVisibility(View.GONE);
            }
        });
    }

    private String createSignInJson(String email, String password) {
        return String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true,\"clientType\":\"CLIENT_TYPE_ANDROID\"}",
                email, password
        );
    }

    private void login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        Retrofit retrofit = AuthApiClient.getAuthClient();
        AuthApiService authApiService = retrofit.create(AuthApiService.class);

        Call<LoginResponse> call = authApiService.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSuccess()) {
                        // Save login data
                        SharedPreferencesUser.saveLoginRequest(requireContext(), request);
                        SharedPreferencesUser.saveLoginResponse(requireContext(), loginResponse);
                        SharedPreferencesUser.saveJWTToken(requireContext(), loginResponse.getToken());
                        SharedPreferencesUser.saveRefreshToken(requireContext(), loginResponse.getRefreshToken());

                        // 🔄 Trigger widget update when user logs in successfully
                        WidgetUpdateHelper.onUserLoginSuccess(requireContext());

                        // Get user profile with JWT token
                        getUserProfile(loginResponse.getToken());
                    } else {
                        showAlertDialog("Đăng nhập thất bại", loginResponse.getMessage());
                    }
                } else {
                    showAlertDialog("Không thể đăng nhập", "Email hoặc mật khẩu không chính xác. Vui lòng thử lại.");
                    Log.e("login", "Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("login", "Network error: " + t.getMessage());
                showErrorDialog("Lỗi kết nối mạng");
            }

        });
    }

    private String createAccountInfoJson(String idToken) {
        return String.format("{\"idToken\":\"%s\"}", idToken);
    }

    private void getUserProfile(String token) {
        Retrofit retrofit = AuthApiClient.getAuthClient();
        AuthApiService authApiService = retrofit.create(AuthApiService.class);
        
        String bearerToken = "Bearer " + token;
        Call<UserProfile> call = authApiService.getUserProfile(bearerToken);
        
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile userProfile = response.body();
                    if (userProfile.getUser() != null && userProfile.getUser().getEmail() != null) {
                        Log.d("BottomSheetChangeEmail", "Profile loaded: " + userProfile.getUser().getEmail());
                        // Note: editTextAccountName is not available in this context, 
                        // this was called after login to transition to ChangeEmailFragment
                        replaceFragmentWithBackStack(new ChangeEmailFragment());
                        
                        tool_bar.setVisibility(View.VISIBLE);
                        txt_continue.setText("Lưu");
                        linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
                        txt_continue.setTextColor(getResources().getColor(R.color.bg));
                        linear_continue.setEnabled(true);
                    }
                } else {
                    Log.e("BottomSheetChangeEmail", "Error loading profile: " + response.code());
                    showErrorDialog("Không thể tải thông tin tài khoản");
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Log.e("BottomSheetChangeEmail", "Network error: " + t.getMessage());
                showErrorDialog("Lỗi kết nối mạng");
            }
        });
    }

    private String createChangeEmailJson(String email) {
        return String.format("{\"email\":\"%s\"}", email);
    }

    private void changeEmail(String email) {
        // ❌ Backend không có endpoint để change email - Tạm thời disable
        Log.w("BottomSheetChangeEmail", "Change email endpoint not implemented in backend");
        showErrorDialog("Tính năng thay đổi email chưa được hỗ trợ");
        return;
        
        /* OLD CODE - Endpoint không tồn tại
        Retrofit retrofit = LoginApiClient.getLoginClient();
        LoginApiService loginApiService = retrofit.create(LoginApiService.class);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), createChangeEmailJson(email));

        Call<ResponseBody> call = loginApiService.ACCOUNT_INFO_RESPONSE_CALL(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String contentEncoding = response.headers().get("Content-Encoding");
                        String responseBody = ResponseUtils.getResponseBody(response.body().byteStream(), contentEncoding);

                        Gson gson = new Gson();
                        AccountInfo accountInfo = gson.fromJson(responseBody, AccountInfo.class);

                        SharedPreferencesUser.saveAccountInfo(requireContext(), accountInfo);

                        replaceFragmentWithBackStack(new ChangeEmailFragment());

                        tool_bar.setVisibility(View.VISIBLE);
                        txt_continue.setText("Lưu");
                        linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
                        txt_continue.setTextColor(getResources().getColor(R.color.bg));
                        linear_continue.setEnabled(true);

                    } catch (IOException e) {
                        Log.e("Auth", "Error reading response body", e);
                    }
                } else {
                    String contentEncoding = response.headers().get("Content-Encoding");
                    try {
                        String responseBody = ResponseUtils.getResponseBody(response.errorBody().byteStream(), contentEncoding);
                        Gson gson = new Gson();
                        LoginError loginError = gson.fromJson(responseBody, LoginError.class);
                        Log.e("login", "onResponse: " + loginError.getError().getMessage().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("login", "Error: " + t.getMessage());
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

    private void replaceFragmentWithBackStack(Fragment newFragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );
        transaction.replace(R.id.frame_layout, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
        return currentFragment;
    }


    @Override
    public void onPasswordChanged(String password) {
        if (password.length() >= 3) {
            linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
            txt_continue.setTextColor(getResources().getColor(R.color.bg));
            linear_continue.setEnabled(true);
        } else {
            linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
            txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
            linear_continue.setEnabled(false);
        }
    }

    private boolean isValidEmail(CharSequence email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onEmailChanged(String email) {
        if (isValidEmail(email)) {
            linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
            txt_continue.setTextColor(getResources().getColor(R.color.bg));
            linear_continue.setEnabled(true);
        } else {
            linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
            txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
            linear_continue.setEnabled(false);
        }
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        BottomSheetInfo bottomSheet1 = new BottomSheetInfo(context, activity);
        bottomSheet1.show(getParentFragmentManager(), bottomSheet1.getTag());
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

