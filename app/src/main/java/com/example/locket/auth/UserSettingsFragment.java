package com.example.locket.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.locket.R;

public class UserSettingsFragment extends Fragment {

    private UserManager userManager;
    private TextView tvCurrentUserType;
    private Button btnSwitchToPro, btnSwitchToFree;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userManager = UserManager.getInstance(requireContext());
        navController = Navigation.findNavController(view);

        tvCurrentUserType = view.findViewById(R.id.tvCurrentUserType);
        btnSwitchToPro = view.findViewById(R.id.btnSwitchToPro);
        btnSwitchToFree = view.findViewById(R.id.btnSwitchToFree);

        updateUI();
        setupButtons();
    }

    private void updateUI() {
        UserType currentType = userManager.getUserType();
        tvCurrentUserType.setText("Loại tài khoản hiện tại: " + currentType.getValue().toUpperCase());

        if (currentType == UserType.PRO) {
            btnSwitchToPro.setEnabled(false);
            btnSwitchToFree.setEnabled(true);
        } else {
            btnSwitchToPro.setEnabled(true);
            btnSwitchToFree.setEnabled(false);
        }
    }

    private void setupButtons() {
        btnSwitchToPro.setOnClickListener(v -> {
            userManager.setUserType(UserType.PRO);
            updateUI();
            Toast.makeText(getContext(), "Đã nâng cấp lên PRO! Bạn có thể quay phim bằng cách giữ nút chụp.", Toast.LENGTH_LONG).show();
        });

        btnSwitchToFree.setOnClickListener(v -> {
            userManager.setUserType(UserType.FREE);
            updateUI();
            Toast.makeText(getContext(), "Đã chuyển về tài khoản FREE", Toast.LENGTH_SHORT).show();
        });
    }
}