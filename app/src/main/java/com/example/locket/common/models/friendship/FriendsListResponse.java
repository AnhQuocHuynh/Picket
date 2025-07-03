package com.example.locket.common.models.friendship;

import java.io.Serializable;
import java.util.List;

public class FriendsListResponse implements Serializable {
    private boolean success;
    private String message;
    private List<FriendData> data;

    public FriendsListResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FriendData> getData() {
        return data;
    }

    public void setData(List<FriendData> data) {
        this.data = data;
    }

    // Nested FriendData class
    public static class FriendData implements Serializable {
        private String _id;
        private String username;
        private String displayName;
        private String profilePicture;
        private String lastMessage;
        private long lastMessageTimestamp;
        private boolean lastMessageUnread;

        public FriendData() {}

        public String getId() {
            return _id;
        }

        public void setId(String id) {
            this._id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public long getLastMessageTimestamp() {
            return lastMessageTimestamp;
        }

        public void setLastMessageTimestamp(long lastMessageTimestamp) {
            this.lastMessageTimestamp = lastMessageTimestamp;
        }

        public boolean isLastMessageUnread() {
            return lastMessageUnread;
        }

        public void setLastMessageUnread(boolean lastMessageUnread) {
            this.lastMessageUnread = lastMessageUnread;
        }
    }
} 