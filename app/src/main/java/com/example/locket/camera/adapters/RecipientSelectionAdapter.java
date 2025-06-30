package com.example.locket.camera.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class RecipientSelectionAdapter extends RecyclerView.Adapter<RecipientSelectionAdapter.ViewHolder> {
    private final Context context;
    private final List<FriendsListResponse.FriendData> friends;
    private final List<FriendsListResponse.FriendData> selectedFriends;
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(FriendsListResponse.FriendData friend, boolean isSelected);
    }

    public RecipientSelectionAdapter(Context context, 
                                   List<FriendsListResponse.FriendData> friends,
                                   List<FriendsListResponse.FriendData> selectedFriends) {
        this.context = context;
        this.friends = friends;
        this.selectedFriends = selectedFriends;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendsListResponse.FriendData friend = friends.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final RoundedImageView img_avatar;
        private final TextView txt_name;
        private final TextView txt_username;
        private final View view_selection_border;
        private final CheckBox checkbox_select;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_avatar = itemView.findViewById(R.id.img_avatar);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_username = itemView.findViewById(R.id.txt_username);
            view_selection_border = itemView.findViewById(R.id.view_selection_border);
            checkbox_select = itemView.findViewById(R.id.checkbox_select);
        }

        public void bind(FriendsListResponse.FriendData friend) {
            // Set display name
            String displayName = friend.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = friend.getUsername() != null ? friend.getUsername() : "Người dùng";
            }
            txt_name.setText(displayName);

            // Set username
            String username = friend.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                txt_username.setText("@" + username);
                txt_username.setVisibility(View.VISIBLE);
            } else {
                txt_username.setVisibility(View.GONE);
            }

            // Load avatar
            if (friend.getProfilePicture() != null && !friend.getProfilePicture().isEmpty()) {
                Glide.with(context)
                        .load(friend.getProfilePicture())
                        .placeholder(R.drawable.ic_widget_empty_icon)
                        .error(R.drawable.ic_widget_empty_icon)
                        .into(img_avatar);
            } else {
                img_avatar.setImageResource(R.drawable.ic_widget_empty_icon);
            }

            // Set selection state
            boolean isSelected = selectedFriends.contains(friend);
            checkbox_select.setChecked(isSelected);
            
            // Show selection border when selected
            if (view_selection_border != null) {
                view_selection_border.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }

            // Set click listeners
            View.OnClickListener clickListener = v -> toggleSelection(friend);
            itemView.setOnClickListener(clickListener);
            checkbox_select.setOnClickListener(clickListener);
        }

        private void toggleSelection(FriendsListResponse.FriendData friend) {
            boolean isCurrentlySelected = selectedFriends.contains(friend);
            
            if (listener != null) {
                listener.onSelectionChanged(friend, !isCurrentlySelected);
            }
            
            // Update checkbox state
            checkbox_select.setChecked(!isCurrentlySelected);
        }
    }
} 