package com.sofiane.newtwitter.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post {
    private String id;
    private String userId;
    private String username;
    private String content;
    private String imageUrl;
    private Date createdAt;
    private int likeCount;
    private int commentCount;

    // Required empty constructor for Firebase
    public Post() {
        // Firebase requires an empty constructor
        this.createdAt = new Date();
    }

    public Post(String id, String userId, String username, String content, String imageUrl, Date createdAt, int likeCount) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.likeCount = likeCount;
        this.commentCount = 0;
    }

    // Method to generate relative time string (e.g., "2 hours ago")
    @Exclude
    public String getRelativeTime() {
        if (createdAt == null) {
            return "just now";
        }
        
        long diffInMillis = new Date().getTime() - createdAt.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;

        if (diffInDays > 0) {
            return diffInDays + " days ago";
        } else if (diffInHours > 0) {
            return diffInHours + " hours ago";
        } else if (diffInMinutes > 0) {
            return diffInMinutes + " minutes ago";
        } else {
            return "just now";
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    // Méthode pour convertir l'objet en Map pour Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("username", username);
        result.put("content", content);
        result.put("imageUrl", imageUrl);
        result.put("createdAt", createdAt.getTime());
        result.put("likeCount", likeCount);
        result.put("commentCount", commentCount);
        
        return result;
    }
} 