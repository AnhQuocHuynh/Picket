package com.example.locket.common.models.friendship;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendRequestResponse {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("data")
    private List<FriendRequest> data;

    public Boolean getSuccess() {
        return success;
    }

    public List<FriendRequest> getData() {
        return data;
    }

    public static class FriendRequest {
        @SerializedName("_id")
        private String id;

        @SerializedName("requester")
        private JsonElement requester;

        @SerializedName("recipient")
        private JsonElement recipient;

        @SerializedName("status")
        private String status;

        @SerializedName("requestMessage")
        private String requestMessage;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("__v")
        private int v;

        public String getId() {
            return id;
        }

        public PersonData getRequesterAsPerson() {
            if (requester != null && requester.isJsonObject()) {
                return new Gson().fromJson(requester, PersonData.class);
            }
            return null;
        }

        public String getRequesterAsString() {
            if (requester != null && requester.isJsonPrimitive()) {
                return requester.getAsString();
            }
            return null;
        }

        public PersonData getRecipientAsPerson() {
            if (recipient != null && recipient.isJsonObject()) {
                return new Gson().fromJson(recipient, PersonData.class);
            }
            return null;
        }

        public String getRecipientAsString() {
            if (recipient != null && recipient.isJsonPrimitive()) {
                return recipient.getAsString();
            }
            return null;
        }

        public String getStatus() {
            return status;
        }

        public String getRequestMessage() {
            return requestMessage;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public int getV() {
            return v;
        }
    }

    public static class PersonData {
        @SerializedName("_id")
        private String id;

        @SerializedName("username")
        private String username;

        @SerializedName("profilePicture")
        private String profilePicture;

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getProfilePicture() {
            return profilePicture;
        }
    }
}