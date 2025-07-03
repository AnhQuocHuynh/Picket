package com.example.locket.profile.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import de.hdodenhof.circleimageview.CircleImageView;
import com.example.locket.R;
import com.example.locket.common.models.friendship.FriendsListResponse;

import java.util.ArrayList;
import java.util.List;

public class NewFriendsAdapter extends RecyclerView.Adapter<NewFriendsAdapter.ViewHolder> {

    public interface OnUnfriendClickListener {
        void onUnfriendClick(String friendId);
    }

    private List<FriendsListResponse.FriendData> friendList;
    private final OnUnfriendClickListener unfriendClickListener;

    public NewFriendsAdapter(List<FriendsListResponse.FriendData> friendList, OnUnfriendClickListener listener) {
        this.friendList = friendList != null ? friendList : new ArrayList<>();
        this.unfriendClickListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateFriendsList(List<FriendsListResponse.FriendData> newFriendList) {
        this.friendList = newFriendList != null ? newFriendList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view, unfriendClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendsListResponse.FriendData friend = friendList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView img_avatar;
        private final TextView txt_full_name;
        private final MaterialButton img_un_friend;
        private final OnUnfriendClickListener listener;

        public ViewHolder(View itemView, OnUnfriendClickListener listener) {
            super(itemView);
            this.listener = listener;
            img_avatar = itemView.findViewById(R.id.img_avatar);
            txt_full_name = itemView.findViewById(R.id.txt_full_name);
            img_un_friend = itemView.findViewById(R.id.img_un_friend);
        }

        public void bind(final FriendsListResponse.FriendData friend) {
            String displayName = friend.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = friend.getUsername() != null ? friend.getUsername() : "User";
            }
            txt_full_name.setText(displayName);

            Glide.with(itemView.getContext())
                    .load(friend.getProfilePicture())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(img_avatar);

            img_un_friend.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUnfriendClick(friend.getId());
                }
            });
        }
    }
}