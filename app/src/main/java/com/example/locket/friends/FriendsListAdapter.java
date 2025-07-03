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
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder> implements Filterable {

    private List<FriendData> friends;
    private List<FriendData> friendsFull;
    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(FriendData friend);
    }

    public FriendsListAdapter(List<FriendData> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.friendsFull = new ArrayList<>(friends);
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

    public void updateFullList(List<FriendData> fullList) {
        this.friendsFull.clear();
        this.friendsFull.addAll(fullList);
    }

    @Override
    public Filter getFilter() {
        return friendFilter;
    }

    private final Filter friendFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<FriendData> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(friendsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (FriendData item : friendsFull) {
                    if (item.getUsername().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            friends.clear();
            friends.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imgAvatar;
        private final TextView txtFullName;
        private final TextView lastMessageTextView;
        private final TextView timestampTextView;
        private final View btnUnfriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtFullName = itemView.findViewById(R.id.txt_full_name);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            btnUnfriend = itemView.findViewById(R.id.img_un_friend);
        }

        public void bind(final FriendData friend, final OnFriendClickListener listener) {
            txtFullName.setText(friend.getUsername());

            if (friend.getLastMessage() != null && !friend.getLastMessage().isEmpty()) {
                lastMessageTextView.setText(friend.getLastMessage());
                lastMessageTextView.setTypeface(null, friend.isLastMessageUnread() ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            } else {
                lastMessageTextView.setText("Bắt đầu trò chuyện!");
                lastMessageTextView.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Simple timestamp formatting (can be improved)
            if (friend.getLastMessageTimestamp() > 0) {
                timestampTextView.setText(android.text.format.DateUtils.getRelativeTimeSpanString(friend.getLastMessageTimestamp()));
            } else {
                timestampTextView.setText("");
            }

            if (friend.getProfilePicture() != null && !friend.getProfilePicture().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(friend.getProfilePicture())
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.empty_icon)
                        .into(imgAvatar);
            }
            btnUnfriend.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> listener.onFriendClick(friend));
        }
    }
}
