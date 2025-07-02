package com.example.locket.common.network;

import com.example.locket.common.models.friendship.FriendRequestResponse;
import com.example.locket.common.models.friendship.FriendshipResponse;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.common.models.friendship.GenerateLinkResponse;
import com.example.locket.common.models.friendship.AcceptLinkRequest;
import com.example.locket.common.models.common.ApiResponse;
import com.example.locket.common.repository.FriendRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FriendshipApiService {

    // 📨 SEND FRIEND REQUEST
    @Headers({
            "Content-Type: application/json"
    })
    @POST("friendship/request")
    Call<FriendshipResponse> sendFriendRequest(
            @Header("Authorization") String bearerToken,
            @Body FriendRequest request
    );

    // ✅ ACCEPT FRIEND REQUEST
    @POST("friendship/accept/{id}")
    Call<FriendshipResponse> acceptFriendRequest(
            @Header("Authorization") String bearerToken,
            @Path("id") String friendshipId
    );

    // ❌ DECLINE FRIEND REQUEST
    @POST("friendship/decline/{id}")
    Call<ApiResponse> declineFriendRequest(
            @Header("Authorization") String bearerToken,
            @Path("id") String friendshipId
    );

    // 🚫 CANCEL SENT FRIEND REQUEST
    @DELETE("friendship/cancel/{id}")
    Call<ApiResponse> cancelFriendRequest(
            @Header("Authorization") String bearerToken,
            @Path("id") String friendshipId
    );

    // 💔 REMOVE FRIEND
    @DELETE("friendship/remove/{id}")
    Call<ApiResponse> removeFriend(
            @Header("Authorization") String bearerToken,
            @Path("id") String friendUserId
    );

    // 👥 GET FRIENDS LIST
    @GET("friendship/list")
    Call<FriendsListResponse> getFriendsList(
            @Header("Authorization") String bearerToken
    );

    // 📥 GET RECEIVED FRIEND REQUESTS (pending)
    @GET("friendship/pending/received")
    Call<FriendRequestResponse> getReceivedFriendRequests(
            @Header("Authorization") String bearerToken
    );

    // 📤 GET SENT FRIEND REQUESTS (pending)
    @GET("friendship/pending/sent")
    Call<FriendRequestResponse> getSentFriendRequests(
            @Header("Authorization") String bearerToken
    );

    // 🔗 GENERATE FRIEND INVITE LINK
    @POST("friendship/generate-link")
    Call<GenerateLinkResponse> generateFriendLink(
            @Header("Authorization") String bearerToken
    );

    // 🎯 ACCEPT FRIEND REQUEST VIA LINK
    @Headers({
            "Content-Type: application/json"
    })
    @POST("friendship/accept-link")
    Call<FriendshipResponse> acceptFriendViaLink(
            @Header("Authorization") String bearerToken,
            @Body AcceptLinkRequest request
    );
} 