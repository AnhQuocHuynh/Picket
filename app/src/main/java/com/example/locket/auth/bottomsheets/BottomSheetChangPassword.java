package com.example.locket.auth.bottomsheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.locket.R;
import com.example.locket.auth.viewmodels.AuthViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class BottomSheetChangPassword extends BottomSheetDialogFragment {

    private AuthViewModel authViewModel;
    private TextInputEditText edtCurrentPassword, edtNewPassword, edtConfirmNewPassword;
    private Button btnChangePassword;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bottomsheet_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize views
        edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = view.findViewById(R.id.edtConfirmNewPassword);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        progressBar = view.findViewById(R.id.progressBar);

        // Set click listener for the change password button
        btnChangePassword.setOnClickListener(v -> handleChangePassword());

        // Observe LiveData
        observeViewModel();
    }

    private void handleChangePassword() {
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        // Validate input fields
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            edtConfirmNewPassword.setError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            edtNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Clear previous errors if any
        edtNewPassword.setError(null);
        edtConfirmNewPassword.setError(null);

        // Call ViewModel to change password
        authViewModel.changePassword(currentPassword, newPassword, confirmNewPassword);
    }

    private void observeViewModel() {
        authViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnChangePassword.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnChangePassword.setEnabled(true);
            }
        });

        authViewModel.successMessage.observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            dismiss();
        });

        authViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
    }
}
