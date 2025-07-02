package com.example.locket.common.models.post;

import java.io.Serializable;
import java.util.List;

public class CategoriesResponse implements Serializable {
    private boolean success;
    private List<CategoryData> data;
    private String message;

    public CategoriesResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<CategoryData> getData() {
        return data;
    }

    public void setData(List<CategoryData> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Category Data class
    public static class CategoryData implements Serializable {
        private String key;       // "all", "art", "animal", etc.
        private String label;     // "Tất cả", "Nghệ thuật", "Động vật", etc.
        private String icon;      // "📸", "🎨", "🐾", etc.
        private int count;        // số lượng posts trong category này

        public CategoryData() {}

        public CategoryData(String key, String label, String icon, int count) {
            this.key = key;
            this.label = label;
            this.icon = icon;
            this.count = count;
        }

        // Getters and Setters
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "CategoryData{" +
                    "key='" + key + '\'' +
                    ", label='" + label + '\'' +
                    ", icon='" + icon + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
} 