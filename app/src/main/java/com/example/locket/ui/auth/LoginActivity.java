package com.example.locket.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.data.model.AuthModels;
import com.example.locket.data.repository.AuthRepository;
import com.example.locket.databinding.ActivityLoginBinding;
import com.example.locket.ui.main.MainActivity;
import com.example.locket.utils.SessionManager;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    
    private ActivityLoginBinding binding;
    
    @Inject
    AuthRepository authRepository;
    
    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            
            if (validateInput(email, password)) {
                performLogin(email, password);
            }
        });

        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Invalid email format");
            return false;
        }

        // Clear previous errors
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
        
        return true;
    }

    private void performLogin(String email, String password) {
        showLoading(true);
        
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthModels.AuthResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    
                    // Check if response has data
                    if (response.getData() != null && response.getData().getUser() != null) {
                        // Save user session
                        sessionManager.saveUserSession(
                            response.getData().getToken(),
                            response.getData().getUser().getId(),
                            response.getData().getUser().getUsername(),
                            response.getData().getUser().getEmail()
                        );
                        
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: Invalid response data", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!show);
        binding.registerButton.setEnabled(!show);
        binding.emailEditText.setEnabled(!show);
        binding.passwordEditText.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 