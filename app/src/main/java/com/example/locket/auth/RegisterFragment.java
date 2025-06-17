package com.example.locket.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.locket.R;

public class RegisterFragment extends Fragment {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvLogin = view.findViewById(R.id.tvLogin);

        // Set click listeners
        btnRegister.setOnClickListener(v -> {
            // TODO: Implement registration validation
            // For now, navigate to home screen
            Navigation.findNavController(view).navigate(R.id.action_register_to_home);
        });

        tvLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_register_to_login);
        });
    }
}
