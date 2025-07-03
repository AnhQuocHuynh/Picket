package com.example.locket.auth.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.locket.R;
import com.example.locket.common.models.auth.LoginRequest;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.network.AuthApiService;
import androidx.lifecycle.ViewModelProvider;

import com.example.locket.auth.bottomsheets.BottomSheetResetPassword;
import com.example.locket.auth.viewmodels.AuthViewModel;
import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.utils.WidgetUpdateHelper;
import com.example.locket.feed.fragments.HomeFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginEmailFragment2 extends Fragment {
    private ImageView img_back;
    private EditText edt_password;
    private TextView txt_forgot_password;
    private TextView txt_forgot_password_send;
    private LinearLayout linear_continue;
    private TextView txt_continue;
    private ImageView img_continue;

    private AuthApiService authApiService;
    private AuthViewModel authViewModel;
    private String data, password;
    private boolean isPhone = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_email2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authApiService = AuthApiClient.getAuthClient().create(AuthApiService.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        conFigViews();
        onClick();
        observeViewModel();
        getDataBundle();

        // Test kết nối khi fragment được tạo
        testConnection();
    }

    private void getDataBundle() {
        if (getArguments() != null) {
            isPhone = getArguments().getBoolean("is_phone");
            data = getArguments().getString("data");
        }
    }

    private void initViews(View view) {
        img_back = view.findViewById(R.id.img_back);
        edt_password = view.findViewById(R.id.edt_password);
        txt_forgot_password = view.findViewById(R.id.txt_forgot_password);
        txt_forgot_password_send = view.findViewById(R.id.txt_forgot_password_send);
        linear_continue = view.findViewById(R.id.linear_continue);
        txt_continue = view.findViewById(R.id.txt_continue);
        img_continue = view.findViewById(R.id.img_continue);
    }

    private void conFigViews() {
        edt_password.requestFocus();

        requireActivity().getWindow().getDecorView().post(() -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edt_password, InputMethodManager.SHOW_IMPLICIT);
        });

        edt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString().trim();

                if (password.length() >= 3) {
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
                    txt_continue.setTextColor(getResources().getColor(R.color.bg));
                    img_continue.setColorFilter(ContextCompat.getColor(requireContext(), R.color.bg));
                    linear_continue.setEnabled(true);
                } else {
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
                    txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
                    img_continue.setColorFilter(ContextCompat.getColor(requireContext(), R.color.hint));
                    linear_continue.setEnabled(false);
                }
            }
        });
    }

    private void onClick() {
        img_back.setOnClickListener(view1 -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        txt_forgot_password.setOnClickListener(view -> forgotPassword(data));
        linear_continue.setOnClickListener(view -> {
            login(data, password);
        });
    }

        private void observeViewModel() {
        authViewModel.successMessage.observe(getViewLifecycleOwner(), message -> {
            showAlertDialog("Success", message);
            // Check if the success message is for the forgot password action
            if (message.toLowerCase().contains("sent to your email")) {
                BottomSheetResetPassword bottomSheet = BottomSheetResetPassword.newInstance();
                bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
            }
        });

        authViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            showAlertDialog("Error", error);
        });

        // You can also observe isLoading to show a progress bar
    }

    private void forgotPassword(String email) {
        if (email != null && !email.isEmpty()) {
            authViewModel.forgotPassword(email);
            BottomSheetResetPassword bottomSheet = BottomSheetResetPassword.newInstance();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        } else {
            showAlertDialog("Error", "Please enter your email first.");
        }
    }

    private void login(String email, String password) {
        // Log chi tiết request để debug
        Log.d("Login", "Attempting login for email: " + email);
        Log.d("Login", "API Base URL: " + AuthApiClient.getAuthClient().baseUrl());

        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = authApiService.login(request);

        Log.d("Login", "Making API call to: " + call.request().url());

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Log chi tiết response để debug
                Log.d("Login", "Response received");
                Log.d("Login", "Response code: " + response.code());
                Log.d("Login", "Response message: " + response.message());
                Log.d("Login", "Request URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d("Login", "Response body received: " + loginResponse.isSuccess());

                    if (loginResponse.isSuccess()) {
                        // Save login data
                        SharedPreferencesUser.saveLoginRequest(requireContext(), request);
                        SharedPreferencesUser.saveLoginResponse(requireContext(), loginResponse);
                        SharedPreferencesUser.saveJWTToken(requireContext(), loginResponse.getToken());
                        SharedPreferencesUser.saveRefreshToken(requireContext(), loginResponse.getRefreshToken());

                        // 🔄 Trigger widget update when user logs in successfully
                        WidgetUpdateHelper.onUserLoginSuccess(requireContext());

                        // Check if user needs to set username
                        if (loginResponse.getUser().getUsername() == null || loginResponse.getUser().getUsername().isEmpty()) {
                            releaseFragment(new RegisterUserNameFragment());
                        } else {
                            releaseFragment(new HomeFragment());
                        }
                    } else {
                        showAlertDialog("Đăng nhập thất bại", loginResponse.getMessage());
                    }
                } else {
                    // Handle error response
                    Log.e("Login", "Error response body: " + response.errorBody());
                    showAlertDialog("Không thể đăng nhập", "Email hoặc mật khẩu không chính xác. Vui lòng thử lại.");
                    Log.e("Login", "Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Log chi tiết lỗi để debug
                Log.e("Login", "Network error: " + t.getMessage());
                Log.e("Login", "Error class: " + t.getClass().getSimpleName());
                Log.e("Login", "Request URL: " + call.request().url());

                if (t.getCause() != null) {
                    Log.e("Login", "Cause: " + t.getCause().getMessage());
                }

                showAlertDialog("Lỗi kết nối", "Vui lòng kiểm tra kết nối internet và thử lại.\nChi tiết: " + t.getMessage());
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

    private void releaseFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );
        transaction.replace(R.id.frame_layout, fragment);
        // Xóa toàn bộ back stack để không quay lại các Fragment trước đó
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.commit();
    }

    // Method test connection đến backend
    private void testConnection() {
        Log.d("ConnectionTest", "🔍 Testing backend connection...");

        // Log base URL để check
        retrofit2.Retrofit client = AuthApiClient.getAuthClient();
        Log.d("ConnectionTest", "📡 Base URL: " + client.baseUrl());
        Log.d("ConnectionTest", "🔧 Network config loaded successfully");

        // Test với một request đơn giản để check server availability
        testServerConnection();
    }

    private void testServerConnection() {
        // ❌ Backend không có check-email endpoint - Test với health check endpoint
        Log.d("ConnectionTest", "🧪 Testing server connection with basic health check...");

        // Test với profile endpoint để check server availability
        retrofit2.Retrofit client = AuthApiClient.getAuthClient();
        Log.d("ConnectionTest", "📡 Testing connection to: " + client.baseUrl());
        Log.d("ConnectionTest", "💡 Make sure backend server is running on port 3000");
        Log.d("ConnectionTest", "💡 Expected URL: http://10.0.2.2:3000/api/");
    }
}