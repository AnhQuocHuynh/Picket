package com.example.locket.common.models.friendship;

import com.google.gson.annotations.SerializedName;

public class FriendRequest {
    @SerializedName("recipientId")
    final String recipientId;
    @SerializedName("message")
    final String message;

    public FriendRequest(String recipientId, String message) {
        this.recipientId = recipientId;
        this.message = message;
    }
}
