package com.example.locket.auth.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.locket.R;
import com.example.locket.common.models.auth.AuthResponse;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.WidgetUpdateHelper;
import com.example.locket.feed.fragments.HomeFragment;

public class RegisterUserNameFragment extends Fragment {
    private static final String TAG = "RegisterUserNameFragment";

    private EditText edt_username;
    private TextView txt_note, txt_check;
    private ProgressBar progress_bar;
    private ImageView img_error, img_close;
    private LinearLayout linear_continue, linear_check;
    private TextView txt_continue;
    private String username;
    private boolean is_check = false;

    // Bundle data từ RegisterEmailFragment
    private String email, password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhận data từ Bundle
        if (getArguments() != null) {
            email = getArguments().getString("email");
            password = getArguments().getString("password");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_user_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        conFigViews();
        onClick();
    }

    private void initViews(View view) {
        img_close = view.findViewById(R.id.img_close);
        edt_username = view.findViewById(R.id.edt_username);
        txt_note = view.findViewById(R.id.txt_note);
        progress_bar = view.findViewById(R.id.progress_bar);
        img_error = view.findViewById(R.id.img_error);
        linear_continue = view.findViewById(R.id.linear_continue);
        txt_continue = view.findViewById(R.id.txt_continue);
        linear_check = view.findViewById(R.id.linear_check);
        txt_check = view.findViewById(R.id.txt_check);
    }

    private void conFigViews() {
        edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                username = s.toString().trim();

                if (username.length() >= 3) {
                    txt_note.setVisibility(View.GONE);
                    linear_check.setVisibility(View.VISIBLE);
                    img_error.setVisibility(View.GONE);
                    progress_bar.setVisibility(View.VISIBLE);
                    txt_check.setText("Đang kiểm tra...");
                    txt_check.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));

                    // Enable continue button
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
                    txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg));
                    linear_continue.setEnabled(true);
                    is_check = true;

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Show username valid
                        progress_bar.setVisibility(View.GONE);
                        img_error.setVisibility(View.GONE);
                        txt_check.setText("Username có thể sử dụng");
                        txt_check.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                        edt_username.setBackgroundResource(R.drawable.background_edit_text);
                    }, 1000);

                } else if (username.isEmpty()) {
                    txt_note.setVisibility(View.VISIBLE);
                    linear_check.setVisibility(View.GONE);
                    edt_username.setBackgroundResource(R.drawable.background_edit_text);

                    // Disable continue button
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
                    txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
                    linear_continue.setEnabled(false);
                    is_check = false;

                } else {
                    txt_note.setVisibility(View.GONE);
                    linear_check.setVisibility(View.VISIBLE);
                    img_error.setVisibility(View.VISIBLE);
                    progress_bar.setVisibility(View.GONE);
                    txt_check.setText("Phải dài hơn 3 ký tự");
                    txt_check.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                    edt_username.setBackgroundResource(R.drawable.background_edit_text_error);

                    // Disable continue button
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
                    txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
                    linear_continue.setEnabled(false);
                    is_check = false;
                }
            }
        });
    }

    private void onClick() {
        linear_continue.setOnClickListener(view -> {
            if (is_check && username.length() >= 3) {
                proceedWithRegistration(username);
            } else {
                showErrorDialog("Vui lòng nhập username hợp lệ (ít nhất 3 ký tự)");
            }
        });

        img_close.setOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void proceedWithRegistration(String username) {
        Log.d(TAG, "🚀 Starting final registration with username: " + username + ", email: " + email);
        if (email == null || password == null) {
            showErrorDialog("Lỗi dữ liệu. Vui lòng thử lại từ đầu.");
            return;
        }
        // Gọi API /api/auth/register
        AuthManager.register(getContext(), username, email, password, new AuthManager.RegisterCallback() {
            @Override
            public void onRegisterSuccess(AuthResponse authResponse) {
                Log.d(TAG, "✅ Registration successful! Chuyển sang xác thực email");
                // Chuyển sang VerifyEmailFragment
                VerifyEmailFragment verifyEmailFragment = new VerifyEmailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                bundle.putString("password", password);
                verifyEmailFragment.setArguments(bundle);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right
                );
                transaction.replace(R.id.frame_layout, verifyEmailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
            @Override
            public void onSuccess(String message) { Log.d(TAG, "✅ Success (Register): " + message); }
            @Override
            public void onError(String errorMessage, int errorCode) { showErrorDialog(errorMessage); }
            @Override
            public void onLoading(boolean isLoading) { /* update UI nếu cần */ }
        });
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Thông báo")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
