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
import com.example.locket.common.models.friendship.FriendRequestResponse;
import com.example.locket.common.models.friendship.FriendsListResponse;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class SentFriendRequestAdapter extends RecyclerView.Adapter<SentFriendRequestAdapter.ViewHolder> {

    private List<FriendRequestResponse.FriendRequest> requests;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onCancel(String friendshipId);
    }

    public SentFriendRequestAdapter(List<FriendRequestResponse.FriendRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request_sent, parent, false);
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

    public void updateRequests(List<FriendRequestResponse.FriendRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imgAvatar;
        private final TextView txtFullName;
        private final Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtFullName = itemView.findViewById(R.id.txt_full_name);
            btnCancel = itemView.findViewById(R.id.btn_cancel_request);
        }

        void bind(final FriendRequestResponse.FriendRequest request, final OnRequestActionListener listener) {
            FriendRequestResponse.PersonData recipient = request.getRecipientAsPerson();
            txtFullName.setText(recipient.getUsername());
            Glide.with(itemView.getContext())
                .load(recipient.getProfilePicture())
                .placeholder(R.mipmap.ic_launcher)
                .into(imgAvatar);

            btnCancel.setOnClickListener(v -> listener.onCancel(request.getId()));
        }
    }
}
