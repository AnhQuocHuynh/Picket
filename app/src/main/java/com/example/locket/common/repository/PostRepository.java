package com.example.locket.common.repository;

import android.content.Context;
import android.util.Log;

import com.example.locket.common.models.post.CreatePostRequest;
import com.example.locket.common.models.post.PostResponse;
import com.example.locket.common.models.post.PostsResponse;
import com.example.locket.common.models.post.LikeResponse;
import com.example.locket.common.models.post.CommentRequest;
import com.example.locket.common.models.post.CommentResponse;
import com.example.locket.common.models.common.ApiResponse;
import com.example.locket.common.network.PostApiService;
import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.ApiErrorHandler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {
    private static final String TAG = "PostRepository";
    private final PostApiService postApiService;
    private final Context context;

    public PostRepository(Context context) {
        this.context = context;
        this.postApiService = AuthApiClient.getAuthClient().create(PostApiService.class);
    }

    // ==================== CALLBACKS ====================
    
    public interface PostsCallback {
        void onSuccess(PostsResponse postsResponse);
        void onError(String message, int code);
        void onLoading(boolean isLoading);
    }

    public interface PostCallback {
        void onSuccess(PostResponse postResponse);
        void onError(String message, int code);
        void onLoading(boolean isLoading);
    }

    public interface LikeCallback {
        void onSuccess(LikeResponse likeResponse);
        void onError(String message, int code);
    }

    public interface CommentCallback {
        void onSuccess(CommentResponse commentResponse);
        void onError(String message, int code);
    }

    public interface DeleteCallback {
        void onSuccess(String message);
        void onError(String message, int code);
    }

    // ==================== POSTS OPERATIONS ====================

    /**
     * 📝 Get all posts (feed) with pagination
     */
    public void getAllPosts(int page, int limit, PostsCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        Call<PostsResponse> call = postApiService.getAllPosts(authHeader, page, limit);
        
        call.enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Get all posts successful - Page: " + page);
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
            public void onFailure(Call<PostsResponse> call, Throwable t) {
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
     * 👥 Get friends posts with pagination
     */
    public void getFriendsPosts(int page, int limit, PostsCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        Call<PostsResponse> call = postApiService.getFriendsPosts(authHeader, page, limit);
        
        call.enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(Call<PostsResponse> call, Response<PostsResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Get friends posts successful - Page: " + page);
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
            public void onFailure(Call<PostsResponse> call, Throwable t) {
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
     * ➕ Create new post
     */
    public void createPost(String imageUrl, String caption, PostCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        if (callback != null) callback.onLoading(true);

        CreatePostRequest request = new CreatePostRequest(imageUrl, caption);
        Call<PostResponse> call = postApiService.createPost(authHeader, request);
        
        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (callback != null) callback.onLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Create post successful");
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
            public void onFailure(Call<PostResponse> call, Throwable t) {
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
     * ❤️ Like/Unlike post
     */
    public void likePost(String postId, LikeCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        Call<LikeResponse> call = postApiService.likePost(authHeader, postId);
        
        call.enqueue(new Callback<LikeResponse>() {
            @Override
            public void onResponse(Call<LikeResponse> call, Response<LikeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Like/unlike post successful");
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
            public void onFailure(Call<LikeResponse> call, Throwable t) {
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
     * 💬 Add comment to post
     */
    public void addComment(String postId, String commentText, CommentCallback callback) {
        String authHeader = AuthManager.getAuthHeader(context);
        if (authHeader == null) {
            if (callback != null) callback.onError("No authentication token", 401);
            return;
        }

        CommentRequest request = new CommentRequest(commentText);
        Call<CommentResponse> call = postApiService.addComment(authHeader, postId, request);
        
        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Add comment successful");
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
            public void onFailure(Call<CommentResponse> call, Throwable t) {
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