package com.example.locket.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.user.UserProfile;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<UserProfile.UserData> userList;
    private OnUserClickListener onUserClickListener;

    public UserSearchAdapter(List<UserProfile.UserData> userList, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile.UserData user = userList.get(position);
        holder.bind(user, onUserClickListener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<UserProfile.UserData> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView usernameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
        }

        public void bind(final UserProfile.UserData user, final OnUserClickListener listener) {
            usernameTextView.setText(user.getUsername());
            Glide.with(itemView.getContext())
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(profileImageView);

            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }

    public interface OnUserClickListener {
        void onUserClick(UserProfile.UserData user);
    }
}
