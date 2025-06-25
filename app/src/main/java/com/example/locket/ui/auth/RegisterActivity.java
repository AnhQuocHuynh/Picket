package com.example.locket.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.data.model.AuthModels;
import com.example.locket.data.repository.AuthRepository;
import com.example.locket.databinding.ActivityRegisterBinding;
import com.example.locket.MainActivity;
import com.example.locket.utils.SessionManager;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {
    
    private ActivityRegisterBinding binding;
    
    @Inject
    AuthRepository authRepository;
    
    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.registerButton.setOnClickListener(v -> {
            String username = binding.usernameEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();
            
            if (validateInput(username, email, password, confirmPassword)) {
                performRegister(username, email, password);
            }
        });

        binding.loginText.setOnClickListener(v -> {
            finish(); // Go back to LoginActivity
        });
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        // Clear previous errors
        binding.usernameLayout.setError(null);
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.confirmPasswordLayout.setError(null);

        if (TextUtils.isEmpty(username)) {
            binding.usernameLayout.setError("Username is required");
            return false;
        }

        if (username.length() < 3) {
            binding.usernameLayout.setError("Username must be at least 3 characters");
            return false;
        }
        
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Invalid email format");
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Please confirm your password");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Passwords do not match");
            return false;
        }
        
        return true;
    }

    private void performRegister(String username, String email, String password) {
        showLoading(true);
        
        // Use username as fullName for now
        String fullName = username;
        
        authRepository.register(username, email, password, fullName, new AuthRepository.AuthCallback() {
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
                        
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: Invalid response data", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.registerButton.setEnabled(!show);
        binding.usernameEditText.setEnabled(!show);
        binding.emailEditText.setEnabled(!show);
        binding.passwordEditText.setEnabled(!show);
        binding.confirmPasswordEditText.setEnabled(!show);
        binding.loginText.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 