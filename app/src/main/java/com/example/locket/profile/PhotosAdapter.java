package com.example.locket.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.Photo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PHOTO = 1;

    private Context context;
    private List<Object> items; // Can be either String (month header) or Photo
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    public PhotosAdapter(Context context, OnPhotoClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        this.listener = listener;
    }

    public void setPhotosWithHeaders(List<Photo> photos) {
        items.clear();

        String currentMonth = "";

        // Group photos by month
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (Photo photo : photos) {
            String month = monthFormat.format(photo.getDate());
            photo.setMonth(month);

            if (!month.equals(currentMonth)) {
                currentMonth = month;
                items.add(month); // Add month header
            }

            items.add(photo); // Add photo
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_PHOTO;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_month_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String month = (String) items.get(position);
            headerHolder.tvMonthHeader.setText(month);
        } else if (holder instanceof PhotoViewHolder) {
            PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
            Photo photo = (Photo) items.get(position);

            // Load image with Glide
            Glide.with(context)
                 .load(photo.getImageUrl())
                 .placeholder(R.drawable.placeholder_image)
                 .into(photoHolder.ivPhotoItem);

            // Set click listener
            photoHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoClick(photo);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonthHeader = itemView.findViewById(R.id.tvMonthHeader);
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhotoItem;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhotoItem = itemView.findViewById(R.id.ivPhotoItem);
        }
    }
}
