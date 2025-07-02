package com.example.locket.common.repository;

public class FriendRequest {
    private String recipientId;
    private String requestMessage;

    public FriendRequest(String recipientId, String requestMessage) {
        this.recipientId = recipientId;
        this.requestMessage = requestMessage;
    }
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
