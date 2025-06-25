package com.example.locket;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.data.api.AuthApi;
import com.example.locket.data.model.AuthModels;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TestNetworkActivity extends AppCompatActivity {
    
    @Inject
    AuthApi authApi;
    
    private TextView statusText;
    private Button testLoginButton;
    private Button testRegisterButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_network);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        statusText = findViewById(R.id.statusText);
        testLoginButton = findViewById(R.id.testLoginButton);
        testRegisterButton = findViewById(R.id.testRegisterButton);
        
        statusText.setText("Backend Connection Tester\nReady to test...");
    }
    
    private void setupClickListeners() {
        testLoginButton.setOnClickListener(v -> testLogin());
        testRegisterButton.setOnClickListener(v -> testRegister());
    }
    
    private void testLogin() {
        statusText.setText("Testing login...");
        
        AuthModels.LoginRequest request = new AuthModels.LoginRequest(
            "test@example.com", 
            "Password123"
        );
        
        Call<AuthModels.AuthResponse> call = authApi.login(request);
        call.enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.AuthResponse authResponse = response.body();
                    
                    String result = "✅ LOGIN SUCCESS!\n" +
                                  "Success: " + authResponse.isSuccess() + "\n" +
                                  "Message: " + authResponse.getMessage() + "\n";
                    
                    if (authResponse.getData() != null) {
                        result += "Token: " + authResponse.getData().getToken() + "\n";
                        if (authResponse.getData().getUser() != null) {
                            result += "User: " + authResponse.getData().getUser().getEmail();
                        }
                    }
                    
                    statusText.setText(result);
                    Toast.makeText(TestNetworkActivity.this, "Login test successful!", Toast.LENGTH_SHORT).show();
                    
                    Log.d("TestNetwork", "Login successful: " + authResponse.getMessage());
                } else {
                    statusText.setText("❌ LOGIN FAILED!\nResponse code: " + response.code());
                    Log.e("TestNetwork", "Login failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                String error = "❌ CONNECTION FAILED!\nError: " + t.getMessage() + 
                              "\n\nPossible issues:\n" +
                              "- Backend server not running\n" +
                              "- Wrong IP address (check 10.0.2.2 for emulator)\n" +
                              "- Network/firewall issues";
                
                statusText.setText(error);
                Toast.makeText(TestNetworkActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                
                Log.e("TestNetwork", "Login connection failed", t);
            }
        });
    }
    
    private void testRegister() {
        statusText.setText("Testing register...");
        
        AuthModels.RegisterRequest request = new AuthModels.RegisterRequest(
            "android_test_new",
            "android_test_new@example.com", 
            "Password123",
            "Android Test"
        );
        
        Call<AuthModels.AuthResponse> call = authApi.register(request);
        call.enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.AuthResponse authResponse = response.body();
                    
                    String result = "✅ REGISTER SUCCESS!\n" +
                                  "Success: " + authResponse.isSuccess() + "\n" +
                                  "Message: " + authResponse.getMessage() + "\n";
                    
                    if (authResponse.getData() != null) {
                        result += "Token: " + authResponse.getData().getToken() + "\n";
                        if (authResponse.getData().getUser() != null) {
                            result += "User: " + authResponse.getData().getUser().getEmail();
                        }
                    }
                    
                    statusText.setText(result);
                    Toast.makeText(TestNetworkActivity.this, "Register test successful!", Toast.LENGTH_SHORT).show();
                    
                    Log.d("TestNetwork", "Register successful: " + authResponse.getMessage());
                } else {
                    statusText.setText("❌ REGISTER FAILED!\nResponse code: " + response.code());
                    Log.e("TestNetwork", "Register failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                String error = "❌ CONNECTION FAILED!\nError: " + t.getMessage() + 
                              "\n\nPossible issues:\n" +
                              "- Backend server not running\n" +
                              "- Wrong IP address (check 10.0.2.2 for emulator)\n" +
                              "- Network/firewall issues";
                
                statusText.setText(error);
                Toast.makeText(TestNetworkActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                
                Log.e("TestNetwork", "Register connection failed", t);
            }
        });
    }
} 