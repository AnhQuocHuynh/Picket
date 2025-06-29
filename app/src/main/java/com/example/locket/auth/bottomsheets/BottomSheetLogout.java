package com.example.locket.auth.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.locket.R;
import com.example.locket.auth.fragments.LoginOrRegisterFragment;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;

public class BottomSheetLogout extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;

    private LinearLayout linear_logout, linear_cancel;

    public BottomSheetLogout(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }
    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme; // Áp dụng theme tùy chỉnh
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.item_bottom_sheet_logout, null);
        bottomSheetDialog.setContentView(view);
        initViews(bottomSheetDialog);
        onClick();

        return bottomSheetDialog;
    }

    private void initViews(BottomSheetDialog bottomSheetDialog) {
        linear_logout = bottomSheetDialog.findViewById(R.id.linear_logout);
        linear_cancel = bottomSheetDialog.findViewById(R.id.linear_cancel);
    }

    private void onClick() {
        linear_logout.setOnClickListener(view -> {
            performLogout();
        });
        linear_cancel.setOnClickListener(view -> {
            dismiss();
        });
    }

    /**
     * 🚪 Perform logout with API call
     */
    private void performLogout() {
        // Show loading state (you can add a progress indicator here)
        linear_logout.setEnabled(false);
        
        AuthManager.logout(requireContext(), new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                // API logout successful
                clearDataAndRedirect();
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                // Even if API logout fails, still clear local data
                // This ensures user is logged out locally even if server is unreachable
                clearDataAndRedirect();
            }

            @Override
            public void onLoading(boolean isLoading) {
                // Handle loading state if needed
                linear_logout.setEnabled(!isLoading);
            }
        });
    }

    /**
     * 🧹 Clear local data and redirect to login
     */
    private void clearDataAndRedirect() {
        SharedPreferencesUser.clearAll(requireContext());
        dismiss();
        releaseFragment();
    }

    private void releaseFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, new LoginOrRegisterFragment());
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.commit();
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismiss();
    }
}
