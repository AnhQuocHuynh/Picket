package com.example.locket.feed.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.common.database.entities.MomentEntity;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ViewAllMomentAdapter extends RecyclerView.Adapter<ViewAllMomentAdapter.ViewHolder> {
    private List<MomentEntity> itemList; // Danh sách URL hình ảnh hoặc dữ liệu
    private final Context context;
    private OnMomentClickListener onMomentClickListener;

    // Interface cho click listener
    public interface OnMomentClickListener {
        void onMomentClick(MomentEntity moment, int position);
        void onCategoryClick(String category);
    }

    public void setOnMomentClickListener(OnMomentClickListener listener) {
        this.onMomentClickListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilterList(List<MomentEntity> filterList) {
        this.itemList = filterList;
        notifyDataSetChanged();
    }

    public ViewAllMomentAdapter(List<MomentEntity> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_moment, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MomentEntity moment = itemList.get(position);

        // 🔧 Load image với CloudinaryImageLoader
        com.example.locket.common.utils.CloudinaryImageLoader.loadThumbnail(
                context,
                moment.getImageUrl(), // Sử dụng imageUrl thay vì thumbnailUrl 
                holder.shapeable_imageview
        );

        // 🏷️ Hiển thị category badge


        // 📱 Handle click events
        holder.itemView.setOnClickListener(v -> {
            if (onMomentClickListener != null) {
                onMomentClickListener.onMomentClick(moment, position);
            }
        });

        // 🏷️ Handle category badge click

    }

    /**
     * 🔍 Extract category from moment data
     * TODO: Replace this with actual category field when available from API
     */
    private String extractCategoryFromMoment(MomentEntity moment) {
        // For now, return a default category or derive from existing data
        // This will be replaced when the Posts API includes category field

        if (moment.getCaption() != null) {
            String caption = moment.getCaption().toLowerCase();

            // Simple keyword matching - to be replaced with actual API category
            if (caption.contains("animal") || caption.contains("động vật") || caption.contains("cat") || caption.contains("dog")) {
                return "Động vật";
            } else if (caption.contains("art") || caption.contains("nghệ thuật") || caption.contains("painting")) {
                return "Nghệ thuật";
            } else if (caption.contains("food") || caption.contains("đồ ăn") || caption.contains("meal")) {
                return "Đồ ăn";
            } else if (caption.contains("landscape") || caption.contains("phong cảnh") || caption.contains("nature")) {
                return "Phong cảnh";
            } else if (caption.contains("people") || caption.contains("con người") || caption.contains("person")) {
                return "Con người";
            }
        }

        // Default category
        return "Khác";
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView shapeable_imageview;

        public ViewHolder(View itemView) {
            super(itemView);
            shapeable_imageview = itemView.findViewById(R.id.shapeable_imageview);
        }
    }
}

