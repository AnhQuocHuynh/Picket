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
import com.example.locket.common.network.AuthApiService;
import com.example.locket.common.network.client.AuthApiClient;

public class LoginEmailFragment extends Fragment {
    private static final String TAG = "LoginEmailFragment";
    private ImageView img_back;
    private EditText edt_email;
    private LinearLayout linear_continue;
    private TextView txt_continue;
    private ImageView img_continue;
    private AuthApiService authApiService;
    private String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authApiService = AuthApiClient.getAuthClient().create(AuthApiService.class);
        initViews(view);
        conFigViews();
        onClick();

        // Test connection để debug
        testBackendConnection();
    }

    private void initViews(View view) {
        img_back = view.findViewById(R.id.img_back);
        edt_email = view.findViewById(R.id.edt_email);
//        login_phone = view.findViewById(R.id.login_phone);
        linear_continue = view.findViewById(R.id.linear_continue);
        txt_continue = view.findViewById(R.id.txt_continue);
        img_continue = view.findViewById(R.id.img_continue);
    }

    private void conFigViews() {
        edt_email.requestFocus(); // Yêu cầu focus vào EditText

        // Đảm bảo rằng bàn phím được mở sau khi focus vào EditText
        requireActivity().getWindow().getDecorView().post(() -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edt_email, InputMethodManager.SHOW_IMPLICIT);
        });
        // Thêm TextWatcher để theo dõi sự thay đổi văn bản trong EditText
        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần thực hiện gì ở đây
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần thực hiện gì ở đây
            }

            @Override
            public void afterTextChanged(Editable s) {
                email = s.toString().trim();

                if (isValidEmail(email)) {
                    // Đổi màu nền và kích hoạt LinearLayout nếu email hợp lệ
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_check));
                    txt_continue.setTextColor(getResources().getColor(R.color.bg));
                    img_continue.setColorFilter(ContextCompat.getColor(requireContext(), R.color.bg)); // Màu tint là màu xanh dương
                    linear_continue.setEnabled(true);
                } else {
                    linear_continue.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_btn_continue_un_check));
                    txt_continue.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint));
                    img_continue.setColorFilter(ContextCompat.getColor(requireContext(), R.color.hint)); // Màu tint là màu xanh dương
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
        linear_continue.setOnClickListener(view -> {
            // Skip email check và chuyển thẳng đến password screen
            // Vì backend không có endpoint /api/auth/check-email
            proceedToPasswordScreen(email);
        });
    }

    private void testBackendConnection() {
        Log.d(TAG, "🔍 Testing backend connection...");
        Log.d(TAG, "📡 Base URL: " + AuthApiClient.getCurrentBaseUrl());

        // Test với endpoint GET /api/auth/profile thay vì check-email
        // Vì backend không có check-email endpoint
        Log.d(TAG, "ℹ️ Backend không có /api/auth/check-email endpoint");
        Log.d(TAG, "ℹ️ Sẽ skip email validation và chuyển thẳng đến password screen");
        Log.d(TAG, "✅ Available endpoints: /api/auth/login, /api/auth/register, /api/auth/profile");
    }

    private void proceedToPasswordScreen(String email) {
        if (!isValidEmail(email)) {
            showErrorDialog("Email không hợp lệ", "Vui lòng nhập email đúng định dạng.", false);
            return;
        }

        Log.d(TAG, "➡️ Proceeding to password screen for email: " + email);
        Log.d(TAG, "ℹ️ Skipping email check vì backend không có endpoint này");
        hideKeyboard();
        releaseFragment(email);
    }

    private void showErrorDialog(String title, String message, boolean showRetry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        if (showRetry) {
            builder.setNeutralButton("Thử lại", (dialog, which) -> {
                dialog.dismiss();
                if (isValidEmail(email)) {
                    proceedToPasswordScreen(email);
                }
            });
        }

        builder.show();
    }

    public void hideKeyboard() {
        // Lấy InputMethodManager
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Kiểm tra xem có view nào đang hiển thị bàn phím không
        View view = getView();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void releaseFragment(String email) {
        // Tạo Bundle và thêm dữ liệu vào đó
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_phone", false); // Ví dụ gửi email
        bundle.putString("data", email); // Ví dụ gửi email

        // Tạo PasswordFragment và thiết lập Bundle
        LoginEmailFragment2 passwordFragment = new LoginEmailFragment2();
        passwordFragment.setArguments(bundle);

        // Thay thế EmailFragment bằng PasswordFragment và thêm vào back stack
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );
        transaction.replace(R.id.frame_layout, passwordFragment);
        transaction.addToBackStack(null); // Thêm vào back stack
        transaction.commit();
    }
    private void releaseFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );
        // transaction.replace(R.id.frame_layout, new LoginPhoneFragment()); // Đã loại bỏ đăng nhập bằng số điện thoại
        // Xóa toàn bộ back stack để không quay lại các Fragment trước đó
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.commit();
    }

    // Phương thức kiểm tra định dạng email
    private boolean isValidEmail(CharSequence email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}