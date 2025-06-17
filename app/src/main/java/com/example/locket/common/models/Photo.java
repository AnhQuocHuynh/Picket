package com.example.locket.common.models;

import java.util.Date;

/**
 * Model class representing a photo in the user's profile
 */
public class Photo {
    private String id;
    private String imageUrl;
    private Date date;
    private String month; // For grouping purposes

    public Photo(String id, String imageUrl, Date date) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    // Empty constructor for Firebase or other data sources
    public Photo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
