package com.sofiane.newtwitter.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String username;
    private String email;
    private String profileImageUrl;
    private String bannerImageUrl;
    private String bio;
    private long createdAt;
    private Map<String, Boolean> followers; // Map des utilisateurs qui suivent cet utilisateur
    private Map<String, Boolean> following; // Map des utilisateurs que cet utilisateur suit
    private int followersCount; // Nombre de followers
    private int followingCount; // Nombre de suivis

    // Required empty constructor for Firestore
    public User() {
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
    }

    public User(String email) {
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
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
    
    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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
    
    // Getters and setters for followers and following
    public Map<String, Boolean> getFollowers() {
        if (followers == null) {
            followers = new HashMap<>();
        }
        return followers;
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
        this.followersCount = followers != null ? followers.size() : 0;
    }

    public Map<String, Boolean> getFollowing() {
        if (following == null) {
            following = new HashMap<>();
        }
        return following;
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
        this.followingCount = following != null ? following.size() : 0;
    }
    
    public int getFollowersCount() {
        return followersCount;
    }
    
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }
    
    public int getFollowingCount() {
        return followingCount;
    }
    
    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
    
    // Helper methods for follow/unfollow operations
    public boolean isFollowing(String userId) {
        return getFollowing().containsKey(userId);
    }
    
    public boolean isFollowedBy(String userId) {
        return getFollowers().containsKey(userId);
    }
    
    public void addFollower(String userId) {
        if (getFollowers().put(userId, true) == null) {
            followersCount++;
        }
    }
    
    public void removeFollower(String userId) {
        if (getFollowers().remove(userId) != null) {
            followersCount--;
        }
    }
    
    public void addFollowing(String userId) {
        if (getFollowing().put(userId, true) == null) {
            followingCount++;
        }
    }
    
    public void removeFollowing(String userId) {
        if (getFollowing().remove(userId) != null) {
            followingCount--;
        }
    }
} 