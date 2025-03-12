package com.sofiane.newtwitter.model;

public class User {
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private long createdAt;

    // Required empty constructor for Firestore
    public User() {}

    public User(String email) {
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Alias method for getUserId() to maintain compatibility
    public String getId() {
        return getUserId();
    }
} 