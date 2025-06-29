package com.example.locket.common.models.friendship;

public class FriendRequest {
    private String recipientId;
    private String requestMessage;

    public FriendRequest() {}

    public FriendRequest(String recipientId, String requestMessage) {
        this.recipientId = recipientId;
        this.requestMessage = requestMessage;
    }

    // Getters and Setters
    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }
} 