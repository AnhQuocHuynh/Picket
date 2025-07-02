package com.example.locket.feed.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFilterAdapter extends RecyclerView.Adapter<CategoryFilterAdapter.ViewHolder> {
    private List<CategoryItem> categories;
    private final Context context;
    private int selectedPosition = 0; // Default to "Tất cả"
    private OnCategorySelectedListener listener;

    // Interface for category selection callback
    public interface OnCategorySelectedListener {
        void onCategorySelected(String category, int position);
    }

    // Data class for category items
    public static class CategoryItem {
        private String name;
        private String icon;
        private int count;

        public CategoryItem(String name, String icon, int count) {
            this.name = name;
            this.icon = icon;
            this.count = count;
        }

        public CategoryItem(String name, int count) {
            this(name, null, count);
        }

        // Getters
        public String getName() { return name; }
        public String getIcon() { return icon; }
        public int getCount() { return count; }

        // Setters
        public void setCount(int count) { this.count = count; }
    }

    public CategoryFilterAdapter(List<CategoryItem> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCategories(List<CategoryItem> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCategoryCounts(Map<String, Integer> categoryCounts) {
        for (CategoryItem item : categories) {
            Integer count = categoryCounts.get(item.getName());
            if (count != null) {
                item.setCount(count);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CategoryItem category = categories.get(position);
        
        // Set category name
        holder.txtCategoryName.setText(category.getName());
        
        // Set category icon if available
        if (category.getIcon() != null && !category.getIcon().isEmpty()) {
            holder.txtCategoryIcon.setText(category.getIcon());
            holder.txtCategoryIcon.setVisibility(View.VISIBLE);
        } else {
            holder.txtCategoryIcon.setVisibility(View.GONE);
        }
        
        // Set count if greater than 0
        if (category.getCount() > 0) {
            holder.txtCategoryCount.setText(String.valueOf(category.getCount()));
            holder.txtCategoryCount.setVisibility(View.VISIBLE);
        } else {
            holder.txtCategoryCount.setVisibility(View.GONE);
        }
        
        // Handle selection state
        boolean isSelected = position == selectedPosition;
        holder.itemView.setSelected(isSelected);
        
        // Update text color based on selection
        int textColor = isSelected ? 
            ContextCompat.getColor(context, R.color.black) : 
            ContextCompat.getColor(context, R.color.text);
        holder.txtCategoryName.setTextColor(textColor);
        
        // Handle click events
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            
            // Notify items for visual update
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            // Callback to listener
            if (listener != null) {
                listener.onCategorySelected(category.getName(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    public void setSelectedCategory(String categoryName) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equals(categoryName)) {
                setSelectedPosition(i);
                return;
            }
        }
        // If category not found, select first item (usually "Tất cả")
        if (categories.size() > 0) {
            setSelectedPosition(0);
        }
    }

    public CategoryItem getCategoryAt(int position) {
        if (position >= 0 && position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        TextView txtCategoryIcon;
        TextView txtCategoryCount;

        public ViewHolder(View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txt_category_name);
            txtCategoryIcon = itemView.findViewById(R.id.txt_category_icon);
            txtCategoryCount = itemView.findViewById(R.id.txt_category_count);
        }
    }

    /**
     * Helper method to create default categories with icons
     */
    public static List<CategoryItem> createDefaultCategories() {
        List<CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryItem("Tất cả", "📸", 0));
        categories.add(new CategoryItem("Nghệ thuật", "🎨", 0));
        categories.add(new CategoryItem("Động vật", "🐾", 0));
        categories.add(new CategoryItem("Con người", "👥", 0));
        categories.add(new CategoryItem("Phong cảnh", "🌄", 0));
        categories.add(new CategoryItem("Đồ ăn", "🍽️", 0));
        categories.add(new CategoryItem("Vui nhộn", "😄", 0));
        categories.add(new CategoryItem("Thời trang", "👗", 0));
        categories.add(new CategoryItem("Thể thao", "⚽", 0));
        categories.add(new CategoryItem("Công nghệ", "💻", 0));
        categories.add(new CategoryItem("Khác", "📋", 0));
        return categories;
    }
} 