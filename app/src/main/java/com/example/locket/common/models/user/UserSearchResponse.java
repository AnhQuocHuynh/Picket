package com.example.locket.common.models.user;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserSearchResponse {

    @SerializedName("data")
    private Data data;

    public List<AccountInfoUser> getUsers() {
        return data != null ? data.getUsers() : null;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @SerializedName("users")
        private List<AccountInfoUser> users;

        public List<AccountInfoUser> getUsers() {
            return users;
        }

        public void setUsers(List<AccountInfoUser> users) {
            this.users = users;
        }
    }
}
