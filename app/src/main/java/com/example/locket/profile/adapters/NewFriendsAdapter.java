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
    private List<FriendsListResponse.FriendData> friendList;
    private final Context context;
    private final Activity activity;

    public NewFriendsAdapter(List<FriendsListResponse.FriendData> friendList, Activity activity, Context context) {
        this.friendList = friendList != null ? friendList : new ArrayList<>();
        this.activity = activity;
        this.context = context;
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
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FriendsListResponse.FriendData friend = friendList.get(position);

        // Set display name or username as fallback
        String displayName = friend.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = friend.getUsername() != null ? friend.getUsername() : "Người dùng";
        }
        holder.txt_full_name.setText(displayName);

        // Load profile picture
        if (friend.getProfilePicture() != null && !friend.getProfilePicture().isEmpty()) {
            Glide.with(context)
                    .load(friend.getProfilePicture())
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.empty_icon)
                    .into(holder.img_avatar);
        } else {
            holder.img_avatar.setImageResource(R.drawable.avatar_placeholder);
        }

        // Remove friend click listener (you can implement this later)
        holder.btn_un_friend.setOnClickListener(view -> {
            // TODO: Implement remove friend functionality
            // You can call FriendshipRepository.removeFriend() here
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView img_avatar;
        private final TextView txt_full_name;
        private final MaterialButton btn_un_friend;

        public ViewHolder(View itemView) {
            super(itemView);
            img_avatar = itemView.findViewById(R.id.img_avatar);
            txt_full_name = itemView.findViewById(R.id.txt_full_name);
            btn_un_friend = itemView.findViewById(R.id.img_un_friend);
        }
    }
} 