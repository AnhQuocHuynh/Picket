package com.example.locket.profile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.friendship.FriendsListResponse;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReceivedFriendRequestAdapter extends RecyclerView.Adapter<ReceivedFriendRequestAdapter.ViewHolder> {

    private List<FriendsListResponse.FriendData> requests;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(String friendshipId);
        void onDecline(String friendshipId);
    }

    public ReceivedFriendRequestAdapter(List<FriendsListResponse.FriendData> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request_received, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(requests.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<FriendsListResponse.FriendData> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imgAvatar;
        private final TextView txtFullName;
        private final Button btnAccept;
        private final Button btnDecline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtFullName = itemView.findViewById(R.id.txt_full_name);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }

        void bind(final FriendsListResponse.FriendData request, final OnRequestActionListener listener) {
            txtFullName.setText(request.getDisplayName());
            Glide.with(itemView.getContext())
                    .load(request.getProfilePicture())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(imgAvatar);

            btnAccept.setOnClickListener(v -> listener.onAccept(request.getId()));
            btnDecline.setOnClickListener(v -> listener.onDecline(request.getId()));
        }
    }
}
