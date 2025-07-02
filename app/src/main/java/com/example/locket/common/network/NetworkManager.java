package com.example.locket.common.network;

import android.content.Context;
import android.util.Log;

import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.post.Post;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    // Use same URL as AuthApiClient for consistency
    private static final String BASE_URL = "http://10.0.2.2:3000"; // For Android Emulator
    // For real device use: http://192.168.1.x:3000 (replace x with your server IP)
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000; // 15 seconds
    private Context context;

    public NetworkManager() {
        // Default constructor
    }

    public NetworkManager(Context context) {
        this.context = context;
    }

    public interface ApiCallback {
        void onSuccess(List<Post> posts);
        void onError(String error);
    }

    public void getLatestPosts(com.example.locket.feed.widget.network.NetworkManager.ApiCallback callback) {
        new Thread(() -> {
            try {
                // Get current user ID to exclude own posts
                String currentUserId = getCurrentUserId();

                // Get auth header using AuthManager
                String authHeader = AuthManager.getAuthHeader(context);
                if (authHeader == null || authHeader.isEmpty()) {
                    Log.e(TAG, "No authentication token found");
                    if (callback != null) {
                        callback.onError("No authentication token found. Please login first.");
                    }
                    return;
                }

                Log.d(TAG, "Fetching friends posts from: " + BASE_URL + "/api/posts/friends");
                Log.d(TAG, "Current user ID: " + currentUserId + " (will be excluded from results)");

                URL url = new URL(BASE_URL + "/api/posts/friends?page=1&limit=10"); // Increase limit to filter properly
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", authHeader);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "API Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String jsonResponse = response.toString();
                    Log.d(TAG, "API Response: " + jsonResponse);

                    // Parse JSON response and filter out current user's posts
                    List<Post> posts = parsePostsResponse(jsonResponse);
                    List<Post> filteredPosts = filterFriendsPosts(posts, currentUserId);

                    if (callback != null) {
                        callback.onSuccess(filteredPosts);
                    }
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.e(TAG, "Unauthorized - Invalid token");
                    if (callback != null) {
                        callback.onError("Authentication failed. Please login again.");
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Log.e(TAG, "API endpoint not found");
                    if (callback != null) {
                        callback.onError("API endpoint not found. Please check server.");
                    }
                } else {
                    // Read error response
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();

                    Log.e(TAG, "HTTP error code: " + responseCode + ", Error: " + errorResponse.toString());
                    if (callback != null) {
                        callback.onError("Server error: " + responseCode);
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching posts: " + e.getMessage(), e);
                if (callback != null) {
                    String errorMessage = "Network error: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("ConnectException")) {
                        errorMessage = "Cannot connect to server. Please check if backend is running.";
                    } else if (e.getMessage() != null && e.getMessage().contains("UnknownHostException")) {
                        errorMessage = "Cannot resolve server address. Please check network connection.";
                    }
                    callback.onError(errorMessage);
                }
            }
        }).start();
    }

    /**
     * Get current user ID from saved login data
     */
    private String getCurrentUserId() {
        if (context == null) return null;

        // Try to get from UserProfile first
        UserProfile userProfile = SharedPreferencesUser.getUserProfile(context);
        if (userProfile != null && userProfile.getUser() != null && userProfile.getUser().getId() != null) {
            Log.d(TAG, "Current user ID from UserProfile: " + userProfile.getUser().getId());
            return userProfile.getUser().getId();
        }

        // Fallback to LoginResponse
        LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(context);
        if (loginResponse != null && loginResponse.getUser() != null && loginResponse.getUser().getId() != null) {
            Log.d(TAG, "Current user ID from LoginResponse: " + loginResponse.getUser().getId());
            return loginResponse.getUser().getId();
        }

        Log.w(TAG, "Could not find current user ID in saved data");
        return null;
    }

    /**
     * Filter out posts from current user to show only friends' posts
     */
    private List<Post> filterFriendsPosts(List<Post> allPosts, String currentUserId) {
        List<Post> friendsPosts = new ArrayList<>();

        if (currentUserId == null) {
            Log.w(TAG, "Current user ID is null, returning all posts");
            return allPosts;
        }

        for (Post post : allPosts) {
            if (post.getUser() != null && post.getUser().getId() != null) {
                if (!currentUserId.equals(post.getUser().getId())) {
                    friendsPosts.add(post);
                    Log.d(TAG, "Added friend's post: " + post.getUser().getUsername());
                } else {
                    Log.d(TAG, "Filtered out own post from: " + post.getUser().getUsername());
                }
            }
        }

        Log.d(TAG, "Filtered " + allPosts.size() + " posts down to " + friendsPosts.size() + " friends' posts");
        return friendsPosts;
    }

    private List<Post> parsePostsResponse(String jsonResponse) throws Exception {
        List<Post> posts = new ArrayList<>();

        JSONObject response = new JSONObject(jsonResponse);
        if (response.getBoolean("success")) {
            JSONArray dataArray = response.getJSONArray("data");

            if (dataArray.length() == 0) {
                Log.d(TAG, "No posts found in response");
                return posts; // Return empty list
            }

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject postJson = dataArray.getJSONObject(i);
                Post post = new Post();

                // Parse post data
                post.setId(postJson.getString("_id"));
                post.setCaption(postJson.optString("caption", ""));

                // Validate and clean image URL
                String imageUrl = postJson.optString("imageUrl", null);
                post.setImageUrl(validateAndCleanImageUrl(imageUrl));

                post.setCategory(postJson.optString("category", "default"));
                post.setCreatedAt(postJson.getString("createdAt"));
                post.setUpdatedAt(postJson.optString("updatedAt", ""));

                // Parse user data
                JSONObject userJson = postJson.getJSONObject("user");
                Post.User user = new Post.User();
                user.setId(userJson.getString("_id"));
                user.setUsername(userJson.getString("username"));

                // Validate and clean profile picture URL
                String profilePicture = userJson.optString("profilePicture", null);
                user.setProfilePicture(validateAndCleanImageUrl(profilePicture));

                post.setUser(user);

                // Parse likes
                JSONArray likesArray = postJson.optJSONArray("likes");
                if (likesArray != null) {
                    List<Post.Like> likes = new ArrayList<>();
                    for (int j = 0; j < likesArray.length(); j++) {
                        JSONObject likeJson = likesArray.getJSONObject(j);
                        Post.Like like = new Post.Like();

                        if (likeJson.has("user") && !likeJson.isNull("user")) {
                            JSONObject likeUserJson = likeJson.getJSONObject("user");
                            Post.User likeUser = new Post.User();
                            likeUser.setId(likeUserJson.getString("_id"));
                            likeUser.setUsername(likeUserJson.getString("username"));
                            like.setUser(likeUser);
                        }
                        like.setCreatedAt(likeJson.optString("likedAt", likeJson.optString("createdAt", "")));

                        likes.add(like);
                    }
                    post.setLikes(likes);
                    post.setLikesCount(likes.size());
                } else {
                    post.setLikesCount(0);
                }

                // Parse comments
                JSONArray commentsArray = postJson.optJSONArray("comments");
                if (commentsArray != null) {
                    List<Post.Comment> comments = new ArrayList<>();
                    for (int j = 0; j < commentsArray.length(); j++) {
                        JSONObject commentJson = commentsArray.getJSONObject(j);
                        Post.Comment comment = new Post.Comment();

                        if (commentJson.has("user") && !commentJson.isNull("user")) {
                            JSONObject commentUserJson = commentJson.getJSONObject("user");
                            Post.User commentUser = new Post.User();
                            commentUser.setId(commentUserJson.getString("_id"));
                            commentUser.setUsername(commentUserJson.getString("username"));
                            comment.setUser(commentUser);
                        }
                        comment.setText(commentJson.optString("text", ""));
                        comment.setCreatedAt(commentJson.getString("createdAt"));

                        comments.add(comment);
                    }
                    post.setComments(comments);
                    post.setCommentsCount(comments.size());
                } else {
                    post.setCommentsCount(0);
                }

                posts.add(post);
                Log.d(TAG, "Parsed post: " + post.getUser().getUsername() + " - " + post.getCaption());
            }
        } else {
            String errorMessage = response.optString("message", "Unknown error");
            throw new Exception("API returned error: " + errorMessage);
        }

        return posts;
    }

    /**
     * Validate and clean image URLs to prevent loading issues
     */
    private String validateAndCleanImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        String cleanUrl = imageUrl.trim();

        // Basic URL validation
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            Log.w(TAG, "Invalid URL protocol, skipping: " + cleanUrl);
            return null;
        }

        // Check for known problematic placeholder services
        if (cleanUrl.contains("via.placeholder.com")) {
            Log.w(TAG, "Detected via.placeholder.com URL which may have DNS issues: " + cleanUrl);
            // You could replace with a more reliable placeholder service here
            // For now, we'll still try to use it but log the warning
        }

        // Basic malformed URL check
        try {
            new java.net.URL(cleanUrl);
        } catch (java.net.MalformedURLException e) {
            Log.e(TAG, "Malformed URL detected, skipping: " + cleanUrl, e);
            return null;
        }

        Log.d(TAG, "Validated image URL: " + cleanUrl);
        return cleanUrl;
    }
}
