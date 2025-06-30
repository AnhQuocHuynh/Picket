package com.example.locket.common.repository;

import android.content.Context;
import android.util.Log;

import com.example.locket.common.models.friendship.AcceptLinkRequest;
import com.example.locket.common.models.friendship.FriendRequest;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.common.models.friendship.FriendshipResponse;
import com.example.locket.common.models.friendship.GenerateLinkResponse;
import com.example.locket.common.network.FriendshipApiService;
import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.utils.ApiErrorHandler;
import com.example.locket.common.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendshipRepository {
    private static final String TAG = "FriendshipRepository";
    private final FriendshipApiService friendshipApiService;
    private final Context context;

    public FriendshipRepository(Context context) {
        this.context = context;
        this.friendshipApiService = AuthApiClient.getAuthClient().create(FriendshipApiService.class);
    }

    // ==================== CALLBACKS ====================
    
    public interface FriendshipCallback {
        void onSuccess(FriendshipResponse friendshipResponse);
        void onError(String message, int code);
        void onLoading(boolean isLoading);
    }

    public interface FriendsListCallback {
        void onSuccess(FriendsListResponse friendsListResponse);
        void onError(String message, int code);
        void onLoading(boolean isLoading);
    }

    public interface LinkCallback {
        void onSuccess(GenerateLinkResponse linkResponse);
        void onError(String message, int code);
    }

    public interface DeleteCallback {
        void onSuccess(String message);
        void onError(String message, int code);
    }

    // ==================== FRIENDSHIP OPERATIONS ====================

    /**
     * 📨 Send friend request
     */
    public void sendFriendRequest(String recipientId, String requestMessage, FriendshipCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        FriendRequest request = new FriendRequest(recipientId, requestMessage);
        Call<FriendshipResponse> call = friendshipApiService.sendFriendRequest(authHeader, request);
        
        call.enqueue(new Callback<FriendshipResponse>() {
            @Override
            public void onResponse(Call<FriendshipResponse> call, Response<FriendshipResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Send friend request successful");
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    ApiErrorHandler.handleError(response, new ApiErrorHandler.ErrorCallback() {
                        @Override
                        public void onError(String message, int code) {
                            if (callback != null) callback.onError(message, code);
                        }

                        @Override
                        public void onTokenExpired() {
                            ApiErrorHandler.clearAuthenticationData(context);
                            if (callback != null) callback.onError("Session expired", 401);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<FriendshipResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                ApiErrorHandler.handleNetworkError(t, new ApiErrorHandler.ErrorCallback() {
                    @Override
                    public void onError(String message, int code) {
                        if (callback != null) callback.onError(message, code);
                    }

                    @Override
                    public void onTokenExpired() {
                        // Not applicable for network errors
                    }
                });
            }
        });
    }

    /**
     * ✅ Accept friend request
     */
    public void acceptFriendRequest(String friendshipId, FriendshipCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        Call<FriendshipResponse> call = friendshipApiService.acceptFriendRequest(authHeader, friendshipId);
        
        call.enqueue(new Callback<FriendshipResponse>() {
            @Override
            public void onResponse(Call<FriendshipResponse> call, Response<FriendshipResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Accept friend request successful");
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    ApiErrorHandler.handleError(response, new ApiErrorHandler.ErrorCallback() {
                        @Override
                        public void onError(String message, int code) {
                            if (callback != null) callback.onError(message, code);
                        }

                        @Override
                        public void onTokenExpired() {
                            ApiErrorHandler.clearAuthenticationData(context);
                            if (callback != null) callback.onError("Session expired", 401);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<FriendshipResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                ApiErrorHandler.handleNetworkError(t, new ApiErrorHandler.ErrorCallback() {
                    @Override
                    public void onError(String message, int code) {
                        if (callback != null) callback.onError(message, code);
                    }

                    @Override
                    public void onTokenExpired() {
                        // Not applicable for network errors
                    }
                });
            }
        });
    }

    /**
     * 👥 Get friends list
     */
    public void getFriendsList(FriendsListCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        Call<FriendsListResponse> call = friendshipApiService.getFriendsList(authHeader);
        
        call.enqueue(new Callback<FriendsListResponse>() {
            @Override
            public void onResponse(Call<FriendsListResponse> call, Response<FriendsListResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Get friends list successful");
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    ApiErrorHandler.handleError(response, new ApiErrorHandler.ErrorCallback() {
                        @Override
                        public void onError(String message, int code) {
                            if (callback != null) callback.onError(message, code);
                        }

                        @Override
                        public void onTokenExpired() {
                            ApiErrorHandler.clearAuthenticationData(context);
                            if (callback != null) callback.onError("Session expired", 401);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<FriendsListResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                ApiErrorHandler.handleNetworkError(t, new ApiErrorHandler.ErrorCallback() {
                    @Override
                    public void onError(String message, int code) {
                        if (callback != null) callback.onError(message, code);
                    }

                    @Override
                    public void onTokenExpired() {
                        // Not applicable for network errors
                    }
                });
            }
        });
    }

    /**
     * 🔗 Generate friend invite link
     */
    public void generateFriendLink(LinkCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        Call<GenerateLinkResponse> call = friendshipApiService.generateFriendLink(authHeader);
        
        call.enqueue(new Callback<GenerateLinkResponse>() {
            @Override
            public void onResponse(Call<GenerateLinkResponse> call, Response<GenerateLinkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Generate friend link successful");
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    ApiErrorHandler.handleError(response, new ApiErrorHandler.ErrorCallback() {
                        @Override
                        public void onError(String message, int code) {
                            if (callback != null) callback.onError(message, code);
                        }

                        @Override
                        public void onTokenExpired() {
                            ApiErrorHandler.clearAuthenticationData(context);
                            if (callback != null) callback.onError("Session expired", 401);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<GenerateLinkResponse> call, Throwable t) {
                ApiErrorHandler.handleNetworkError(t, new ApiErrorHandler.ErrorCallback() {
                    @Override
                    public void onError(String message, int code) {
                        if (callback != null) callback.onError(message, code);
                    }

                    @Override
                    public void onTokenExpired() {
                        // Not applicable for network errors
                    }
                });
            }
        });
    }

    /**
     * 🎯 Accept friend request via link
     */
    public void acceptFriendViaLink(String token, FriendshipCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        AcceptLinkRequest request = new AcceptLinkRequest(token);
        Call<FriendshipResponse> call = friendshipApiService.acceptFriendViaLink(authHeader, request);
        
        call.enqueue(new Callback<FriendshipResponse>() {
            @Override
            public void onResponse(Call<FriendshipResponse> call, Response<FriendshipResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Accept friend via link successful");
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    ApiErrorHandler.handleError(response, new ApiErrorHandler.ErrorCallback() {
                        @Override
                        public void onError(String message, int code) {
                            if (callback != null) callback.onError(message, code);
                        }

                        @Override
                        public void onTokenExpired() {
                            ApiErrorHandler.clearAuthenticationData(context);
                            if (callback != null) callback.onError("Session expired", 401);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<FriendshipResponse> call, Throwable t) {
                if (callback != null) callback.onLoading(false);
                ApiErrorHandler.handleNetworkError(t, new ApiErrorHandler.ErrorCallback() {
                    @Override
                    public void onError(String message, int code) {
                        if (callback != null) callback.onError(message, code);
                    }

                    @Override
                    public void onTokenExpired() {
                        // Not applicable for network errors
                    }
                });
            }
        });
    }
} 