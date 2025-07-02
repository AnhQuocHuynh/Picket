package com.example.locket.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.friendship.FriendsListResponse.FriendData;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder> {

    private List<FriendData> friends;
    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(FriendData friend);
    }

    public FriendsListAdapter(List<FriendData> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friends.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imgAvatar;
        private final TextView txtFullName;
        private final View btnUnfriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtFullName = itemView.findViewById(R.id.txt_full_name);
            btnUnfriend = itemView.findViewById(R.id.img_un_friend);
        }

        public void bind(final FriendData friend, final OnFriendClickListener listener) {
            txtFullName.setText(friend.getUsername());
            // Load profile picture
            if (friend.getProfilePicture() != null && !friend.getProfilePicture().isEmpty())
            {
            Glide.with(itemView.getContext())
                    .load(friend.getProfilePicture())
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.empty_icon)
                    .into(imgAvatar);
            }
            // Ẩn nút hủy kết bạn vì chúng ta chỉ muốn chọn để chat
            btnUnfriend.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> listener.onFriendClick(friend));
        }
    }
}
