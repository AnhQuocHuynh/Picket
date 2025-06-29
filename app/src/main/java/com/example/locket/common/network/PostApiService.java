package com.example.locket.common.network;

import com.example.locket.common.models.post.CreatePostRequest;
import com.example.locket.common.models.post.PostResponse;
import com.example.locket.common.models.post.PostsResponse;
import com.example.locket.common.models.post.LikeResponse;
import com.example.locket.common.models.post.CommentRequest;
import com.example.locket.common.models.post.CommentResponse;
import com.example.locket.common.models.common.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostApiService {

    // 📝 GET ALL POSTS (Feed) - with pagination
    @GET("posts")
    Call<PostsResponse> getAllPosts(
            @Header("Authorization") String bearerToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // 👥 GET FRIENDS POSTS - with pagination  
    @GET("posts/friends")
    Call<PostsResponse> getFriendsPosts(
            @Header("Authorization") String bearerToken,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // 🏷️ GET POSTS BY CATEGORY - with pagination
    @GET("posts/category/{categoryName}")
    Call<PostsResponse> getPostsByCategory(
            @Header("Authorization") String bearerToken,
            @Path("categoryName") String categoryName,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // ➕ CREATE NEW POST
    @Headers({
            "Content-Type: application/json"
    })
    @POST("posts")
    Call<PostResponse> createPost(
            @Header("Authorization") String bearerToken,
            @Body CreatePostRequest request
    );

    // 🔍 GET SINGLE POST BY ID
    @GET("posts/{id}")
    Call<PostResponse> getPostById(
            @Header("Authorization") String bearerToken,
            @Path("id") String postId
    );

    // ✏️ UPDATE POST CAPTION
    @Headers({
            "Content-Type: application/json"
    })
    @PUT("posts/{id}")
    Call<PostResponse> updatePost(
            @Header("Authorization") String bearerToken,
            @Path("id") String postId,
            @Body CreatePostRequest request
    );

    // 🗑️ DELETE POST
    @DELETE("posts/{id}")
    Call<ApiResponse> deletePost(
            @Header("Authorization") String bearerToken,
            @Path("id") String postId
    );

    // ❤️ LIKE/UNLIKE POST
    @POST("posts/{id}/like")
    Call<LikeResponse> likePost(
            @Header("Authorization") String bearerToken,
            @Path("id") String postId
    );

    // 💬 ADD COMMENT
    @Headers({
            "Content-Type: application/json"
    })
    @POST("posts/{id}/comment")
    Call<CommentResponse> addComment(
            @Header("Authorization") String bearerToken,
            @Path("id") String postId,
            @Body CommentRequest request
    );

    // 👤 GET POSTS BY USER ID
    @GET("posts/user/{userId}")
    Call<PostsResponse> getPostsByUser(
            @Header("Authorization") String bearerToken,
            @Path("userId") String userId,
            @Query("page") int page,
            @Query("limit") int limit
    );
} 