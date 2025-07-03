package com.example.locket.common.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.locket.common.database.Converters;

import java.util.List;

@Entity(tableName = "moment_table")
@TypeConverters({Converters.class}) // Sử dụng để chuyển đổi List<Overlay> sang String và ngược lại
public class MomentEntity {

    @PrimaryKey
    @NonNull
    public String id; // Post ID from API

    public String canonicalUid; // Legacy field, can be same as id
    public String user; // Username from Post.User
    public String thumbnailUrl; // Legacy field
    public String imageUrl; // Main image URL from Post
    public long dateSeconds; // lưu trường _seconds của đối tượng Date
    public long timestamp; // Full timestamp in milliseconds
    public String caption; // Caption from Post
    public String category; // Category from Post (AI classified)
    public String md5; // Legacy field
    public List<Overlay> overlays; // Overlays for captions

    // Default constructor for Room
    public MomentEntity() {
        this.id = "";
    }

    // Constructor with parameters - ignored by Room
    @Ignore
    public MomentEntity(@NonNull String id, String user, String imageUrl, String thumbnailUrl, long dateSeconds, long timestamp, String caption, String category, String md5, List<Overlay> overlays) {
        this.id = id;
        this.canonicalUid = id; // Use same as id for compatibility
        this.user = user;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.dateSeconds = dateSeconds;
        this.timestamp = timestamp;
        this.caption = caption;
        this.category = category;
        this.md5 = md5;
        this.overlays = overlays;
    }

    // Getter và Setter
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getCanonicalUid() {
        return canonicalUid != null ? canonicalUid : id;
    }

    public void setCanonicalUid(@NonNull String canonicalUid) {
        this.canonicalUid = canonicalUid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl != null ? thumbnailUrl : imageUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getDateSeconds() {
        return dateSeconds;
    }

    public void setDateSeconds(long dateSeconds) {
        this.dateSeconds = dateSeconds;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public List<Overlay> getOverlays() {
        return overlays;
    }

    public void setOverlays(List<Overlay> overlays) {
        this.overlays = overlays;
    }

    // Nested Overlay class for captions and decorations
    public static class Overlay {
        public String overlay_id;
        public String alt_text;

        public Overlay() {}

        public Overlay(String overlay_id, String alt_text) {
            this.overlay_id = overlay_id;
            this.alt_text = alt_text;
        }

        public String getOverlay_id() {
            return overlay_id;
        }

        public void setOverlay_id(String overlay_id) {
            this.overlay_id = overlay_id;
        }

        public String getAlt_text() {
            return alt_text;
        }

        public void setAlt_text(String alt_text) {
            this.alt_text = alt_text;
        }
    }
}
