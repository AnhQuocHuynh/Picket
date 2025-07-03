package com.example.locket.auth.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.locket.R;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.feed.fragments.HomeFragment;

public class VerifyEmailFragment extends Fragment {
    private EditText etCode;
    private Button btnVerify, btnResend;
    private TextView tvError;
    private String email; // Email nhận từ bundle arguments
    private String password; // Lưu password để tự động đăng nhập
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_email, container, false);
        etCode = view.findViewById(R.id.et_code);
        btnVerify = view.findViewById(R.id.btn_verify);
        btnResend = view.findViewById(R.id.btn_resend);
        tvError = view.findViewById(R.id.tv_error);

        if (getArguments() != null) {
            email = getArguments().getString("email");
            password = getArguments().getString("password"); // Nhận password từ bundle nếu có
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        btnVerify.setOnClickListener(v -> onVerifyClicked());
        btnResend.setOnClickListener(v -> onResendClicked());

        return view;
    }

    private void onVerifyClicked() {
        String code = etCode.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() < 4) {
            showError("Vui lòng nhập mã xác thực hợp lệ.");
            return;
        }
        tvError.setVisibility(View.GONE);
        progressDialog.show();
        AuthManager.verifyEmail(getContext(), email, code, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                // Xác thực thành công, tự động đăng nhập
                AuthManager.login(getContext(), email, password, new AuthManager.LoginCallback() {
                    @Override
                    public void onLoginSuccess(LoginResponse loginResponse) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển sang HomeFragment (hoặc MainActivity tuỳ flow)
                        requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_layout, new HomeFragment())
                            .commit();
                    }
                    @Override
                    public void onSuccess(String msg) {}
                    @Override
                    public void onError(String errorMessage, int errorCode) {
                        progressDialog.dismiss();
                        showError("Đăng nhập tự động thất bại: " + errorMessage);
                    }
                    @Override
                    public void onLoading(boolean isLoading) {
                        if (isLoading) progressDialog.show();
                        else progressDialog.dismiss();
                    }
                });
            }
            @Override
            public void onError(String errorMessage, int errorCode) {
                progressDialog.dismiss();
                showError(errorMessage);
            }
            @Override
            public void onLoading(boolean isLoading) {
                if (isLoading) progressDialog.show();
                else progressDialog.dismiss();
            }
        });
    }

    private void onResendClicked() {
        tvError.setVisibility(View.GONE);
        progressDialog.show();
        
        // Lấy token từ đăng ký
        String token = SharedPreferencesUser.getJWTToken(getContext());
        if (token != null) {
            // Gọi API với token
            AuthManager.resendVerificationWithToken(getContext(), email, token, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Đã gửi lại mã xác thực!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String errorMessage, int errorCode) {
                    progressDialog.dismiss();
                    showError(errorMessage);
                }
                @Override
                public void onLoading(boolean isLoading) {
                    if (isLoading) progressDialog.show();
                    else progressDialog.dismiss();
                }
            });
        } else {
            progressDialog.dismiss();
            showError("Không thể gửi lại mã. Vui lòng thử lại.");
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
} 