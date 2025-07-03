package com.example.locket.auth.bottomsheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.locket.R;
import com.example.locket.auth.viewmodels.AuthViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetResetPassword extends BottomSheetDialogFragment {

    private EditText edtCode, edtNewPassword, edtConfirmPassword;
    private Button btnResetPassword;
    private AuthViewModel authViewModel;

    public static BottomSheetResetPassword newInstance() {
        return new BottomSheetResetPassword();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupListeners();
    }

    private void initViews(View view) {
        edtCode = view.findViewById(R.id.edt_code);
        edtNewPassword = view.findViewById(R.id.edt_new_password);
        edtConfirmPassword = view.findViewById(R.id.edt_confirm_password);
        btnResetPassword = view.findViewById(R.id.btn_reset_password);
    }

    private void initViewModel() {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.successMessage.observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            if (message.toLowerCase().contains("reset successfully")) {
                dismiss();
            }
        });

        authViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        authViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            btnResetPassword.setEnabled(!isLoading);
            btnResetPassword.setText(isLoading ? "Resetting..." : "Reset Password");
        });
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (validateInput(code, newPassword, confirmPassword)) {
                authViewModel.resetPassword(code, newPassword, confirmPassword);
            }
        });
    }

    private boolean validateInput(String code, String newPassword, String confirmPassword) {
        if (code.isEmpty()) {
            edtCode.setError("Code is required");
            return false;
        }
        if (newPassword.isEmpty()) {
            edtNewPassword.setError("New password is required");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}
