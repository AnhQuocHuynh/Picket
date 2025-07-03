package com.example.locket.chat.model;

public class ChatMessage {

    public enum MessageType {
        TEXT, IMAGE, VIDEO, MOMENT_COMMENT
    }

    private String text;
    private String senderId;
    private String receiverId;
    private long timestamp;
    private MessageType type;
    private String mediaUrl;
    
    // Moment comment specific fields
    private String momentId;
    private String momentImageUrl;
    private String momentOwnerId;

    // Constructor mặc định là bắt buộc để Firebase có thể chuyển đổi dữ liệu
    public ChatMessage() {
    }

    // Constructor for text messages
    public ChatMessage(String text, String senderId, String receiverId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.type = MessageType.TEXT;
    }

    // Constructor for media messages
    public ChatMessage(String senderId, String receiverId, long timestamp, MessageType type, String mediaUrl) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.text = ""; // Or a caption if you want
    }

    // Constructor for moment comment messages
    public ChatMessage(String commentText, String senderId, String receiverId, long timestamp, 
                      String momentId, String momentImageUrl, String momentOwnerId) {
        this.text = commentText;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.type = MessageType.MOMENT_COMMENT;
        this.momentId = momentId;
        this.momentImageUrl = momentImageUrl;
        this.momentOwnerId = momentOwnerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Moment comment specific getters and setters
    public String getMomentId() {
        return momentId;
    }

    public void setMomentId(String momentId) {
        this.momentId = momentId;
    }

    public String getMomentImageUrl() {
        return momentImageUrl;
    }

    public void setMomentImageUrl(String momentImageUrl) {
        this.momentImageUrl = momentImageUrl;
    }

    public String getMomentOwnerId() {
        return momentOwnerId;
    }

    public void setMomentOwnerId(String momentOwnerId) {
        this.momentOwnerId = momentOwnerId;
    }
}
