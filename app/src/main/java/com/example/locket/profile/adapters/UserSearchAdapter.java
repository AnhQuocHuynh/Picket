package com.example.locket.profile.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.user.AccountInfoUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private final List<AccountInfoUser> userList;
    private final OnAddFriendClickListener listener;

    public interface OnAddFriendClickListener {
        void onAddFriendClick(String userId);
    }

    public UserSearchAdapter(List<AccountInfoUser> userList, OnAddFriendClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountInfoUser user = userList.get(position);
        holder.tvUsername.setText(user.getDisplayName());

        Glide.with(holder.itemView.getContext())
                .load(user.getPhotoUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.imgAvatar);

        holder.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddFriendClick(user.getLocalId());
                holder.btnAddFriend.setEnabled(false);
                holder.btnAddFriend.setText("Sent");
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateUsers(List<AccountInfoUser> newUsers) {
        this.userList.clear();
        if (newUsers != null) {
            this.userList.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView imgAvatar;
        final TextView tvUsername;
        final Button btnAddFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
        }
    }
}
