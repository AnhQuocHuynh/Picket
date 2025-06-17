package com.example.locket;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class TestNetworkActivity extends AppCompatActivity {
    private static final String TAG = "TestNetwork";
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple layout
        setContentView(android.R.layout.activity_list_item);
        resultText = findViewById(android.R.id.text1);
        
        Button testButton = new Button(this);
        testButton.setText("Test Network");
        testButton.setOnClickListener(v -> testNetwork());
        
        // Add button to layout (simplified)
        resultText.setText("Click test to check network");
    }

    private void testNetwork() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:3000/api/health")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network test failed", e);
                runOnUiThread(() -> resultText.setText("FAILED: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Network test success: " + responseBody);
                runOnUiThread(() -> resultText.setText("SUCCESS: " + responseBody));
            }
        });
    }
} 