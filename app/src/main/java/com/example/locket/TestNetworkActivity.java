package com.example.locket;

import android.content.Intent;
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
    private Button openMainAppButton;
    
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
        openMainAppButton = findViewById(R.id.openMainAppButton);
        
        statusText.setText("Backend Connection Tester\nReady to test...");
    }
    
    private void setupClickListeners() {
        testLoginButton.setOnClickListener(v -> testLogin());
        testRegisterButton.setOnClickListener(v -> testRegister());
        openMainAppButton.setOnClickListener(v -> openMainApp());
    }
    
    private void openMainApp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        // Don't finish() so user can come back to testing
    }
    
    private void testLogin() {
        statusText.setText("Testing login...");
        
        AuthModels.LoginRequest request = new AuthModels.LoginRequest(
            "testuser@backend.com", 
            "Password123"
        );
        
        Call<AuthModels.AuthResponse> call = authApi.login(request);
        call.enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                Log.d("TestNetwork", "Login URL: " + call.request().url());
                Log.d("TestNetwork", "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.AuthResponse authResponse = response.body();
                    
                    String result = "✅ LOGIN SUCCESS!\n" +
                                  "URL: " + call.request().url() + "\n" +
                                  "Success: " + authResponse.isSuccess() + "\n" +
                                  "Message: " + authResponse.getMessage() + "\n";
                    
                    if (authResponse.getData() != null) {
                        result += "Token: " + authResponse.getData().getToken().substring(0, 30) + "...\n";
                        if (authResponse.getData().getUser() != null) {
                            result += "User: " + authResponse.getData().getUser().getEmail();
                        }
                    }
                    
                    statusText.setText(result);
                    Toast.makeText(TestNetworkActivity.this, "Login test successful!", Toast.LENGTH_SHORT).show();
                    
                    Log.d("TestNetwork", "Login successful: " + authResponse.getMessage());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "Could not read error";
                    }
                    
                    String result = "❌ LOGIN FAILED!\n" +
                                  "URL: " + call.request().url() + "\n" +
                                  "Response code: " + response.code() + "\n" +
                                  "Error: " + errorBody;
                    
                    statusText.setText(result);
                    Log.e("TestNetwork", "Login failed: " + response.code() + " - " + errorBody);
                }
            }
            
            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                String error = "❌ CONNECTION FAILED!\n" +
                              "URL: " + call.request().url() + "\n" +
                              "Error: " + t.getMessage() + 
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
        
        // Tạo username và email unique mỗi lần test (short username for validation)
        long timestamp = System.currentTimeMillis();
        String uniqueUsername = "usr" + (timestamp % 1000000); // Giới hạn 6 số cuối để username ngắn
        String uniqueEmail = "android_" + timestamp + "@example.com";
        
        AuthModels.RegisterRequest request = new AuthModels.RegisterRequest(
            uniqueUsername,
            uniqueEmail, 
            "Password123",
            "Android Test User"
        );
        
        Call<AuthModels.AuthResponse> call = authApi.register(request);
        call.enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                Log.d("TestNetwork", "Register URL: " + call.request().url());
                Log.d("TestNetwork", "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthModels.AuthResponse authResponse = response.body();
                    
                    String result = "✅ REGISTER SUCCESS!\n" +
                                  "URL: " + call.request().url() + "\n" +
                                  "Success: " + authResponse.isSuccess() + "\n" +
                                  "Message: " + authResponse.getMessage() + "\n";
                    
                    if (authResponse.getData() != null) {
                        result += "Token: " + authResponse.getData().getToken().substring(0, 30) + "...\n";
                        if (authResponse.getData().getUser() != null) {
                            result += "User: " + authResponse.getData().getUser().getEmail();
                        }
                    }
                    
                    statusText.setText(result);
                    Toast.makeText(TestNetworkActivity.this, "Register test successful!", Toast.LENGTH_SHORT).show();
                    
                    Log.d("TestNetwork", "Register successful: " + authResponse.getMessage());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "Could not read error";
                    }
                    
                    String result = "❌ REGISTER FAILED!\n" +
                                  "URL: " + call.request().url() + "\n" +
                                  "Response code: " + response.code() + "\n" +
                                  "Error: " + errorBody;
                    
                    statusText.setText(result);
                    Log.e("TestNetwork", "Register failed: " + response.code() + " - " + errorBody);
                }
            }
            
            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                String error = "❌ CONNECTION FAILED!\n" +
                              "URL: " + call.request().url() + "\n" +
                              "Error: " + t.getMessage() + 
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