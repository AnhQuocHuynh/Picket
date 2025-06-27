package com.example.locket.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
// import com.bumptech.glide.Glide; // Bạn sẽ cần thêm thư viện Glide để tải ảnh

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final List<Post> posts;

    public FeedAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPostImage;
        private final TextView tvCaption;
        private final ImageView ivUserAvatar;
        private final TextView tvUsername;
        private final TextView tvPostTime;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
        }

        public void bind(Post post) {
            tvCaption.setText(post.getCaption());
            tvUsername.setText(post.getUsername());
            tvPostTime.setText(post.getPostTime());

            // Để tải ảnh từ URL, bạn nên dùng thư viện như Glide hoặc Picasso.
            // Ví dụ với Glide:
            // Glide.with(itemView.getContext()).load(post.getImageUrl()).into(ivPostImage);
            // Glide.with(itemView.getContext()).load(post.getUserAvatarUrl()).into(ivUserAvatar);

            // Tạm thời, chúng ta có thể đặt ảnh mẫu
            ivPostImage.setImageResource(android.R.drawable.screen_background_light);
            ivUserAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }
}
