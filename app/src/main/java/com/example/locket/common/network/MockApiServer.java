package com.example.locket.common.network;

import android.os.Handler;
import android.os.Looper;
import com.example.locket.common.utils.MockDataService;
import okhttp3.ResponseBody;
import okhttp3.MediaType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MockApiServer {
    
    private static final int MOCK_DELAY_MS = 1000; // Simulate network delay
    
    public static class MockCall<T> implements Call<T> {
        private final T mockResponse;
        private final boolean shouldSucceed;
        private final int responseCode;
        
        public MockCall(T mockResponse, boolean shouldSucceed, int responseCode) {
            this.mockResponse = mockResponse;
            this.shouldSucceed = shouldSucceed;
            this.responseCode = responseCode;
        }
        
        @Override
        public void enqueue(Callback<T> callback) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (shouldSucceed) {
                    Response<T> response = Response.success(responseCode, mockResponse);
                    callback.onResponse(this, response);
                } else {
                    callback.onFailure(this, new Exception("Mock API Error"));
                }
            }, MOCK_DELAY_MS);
        }
        
        @Override
        public Response<T> execute() {
            try {
                Thread.sleep(MOCK_DELAY_MS);
                if (shouldSucceed) {
                    return Response.success(responseCode, mockResponse);
                } else {
                    return Response.error(500, ResponseBody.create(
                        MediaType.parse("application/json"), 
                        "{\"error\":\"Mock API Error\"}"
                    ));
                }
            } catch (InterruptedException e) {
                return Response.error(500, ResponseBody.create(
                    MediaType.parse("application/json"), 
                    "{\"error\":\"" + e.getMessage() + "\"}"
                ));
            }
        }
        
        @Override public boolean isExecuted() { return false; }
        @Override public void cancel() { }
        @Override public boolean isCanceled() { return false; }
        @Override public Call<T> clone() { return new MockCall<>(mockResponse, shouldSucceed, responseCode); }
        @Override public okhttp3.Request request() { return null; }
        @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
    }
    
    // Mock Login API
    public static Call<ResponseBody> getMockLoginResponse() {
        String jsonResponse = MockDataService.getMockLoginApiResponse();
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        return new MockCall<>(responseBody, true, 200);
    }
    
    // Mock Check Email API  
    public static Call<ResponseBody> getMockCheckEmailResponse() {
        String jsonResponse = MockDataService.getMockCheckEmailResponse();
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        return new MockCall<>(responseBody, true, 200);
    }
    
    // Mock Get Moments API
    public static Call<ResponseBody> getMockMomentsResponse() {
        String jsonResponse = MockDataService.getMockMomentsApiResponse();
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        return new MockCall<>(responseBody, true, 200);
    }
    
    // Mock Get Friends API
    public static Call<ResponseBody> getMockFriendsResponse() {
        String jsonResponse = MockDataService.getMockFriendsApiResponse();
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        return new MockCall<>(responseBody, true, 200);
    }
    
    // Mock Upload Image API
    public static Call<ResponseBody> getMockUploadImageResponse() {
        String jsonResponse = "{\n" +
                "  \"success\": true,\n" +
                "  \"downloadTokens\": \"mock_download_token_12345\",\n" +
                "  \"url\": \"https://picsum.photos/400/400?random=" + System.currentTimeMillis() + "\"\n" +
                "}";
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        return new MockCall<>(responseBody, true, 200);
    }
    
    // Mock Post Moment API
    public static Call<ResponseBody> getMockPostMomentResponse() {
        String jsonResponse = "{\n" +
                "  \"success\": true,\n" +
                "  \"moment_id\": \"moment_" + System.currentTimeMillis() + "\",\n" +
                "  \"message\": \"Moment posted successfully\"\n" +
                "}";
        ResponseBody responseBody = ResponseBody.create(
            MediaType.parse("application/json"), 
            jsonResponse
        );
        return new MockCall<>(responseBody, true, 200);
    }
} 