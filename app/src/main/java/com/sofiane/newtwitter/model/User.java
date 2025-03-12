package com.sofiane.newtwitter.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String id; // Ajout d'un champ id pour la compatibilité avec Firebase
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
    private int profileIconIndex; // Index de l'icône de profil
    private int profileColorIndex; // Index de la couleur de profil

    // Required empty constructor for Firestore
    public User() {
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
        this.profileIconIndex = 0; // Icône par défaut
        this.profileColorIndex = 0; // Couleur par défaut
    }

    public User(String email) {
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
        this.profileIconIndex = 0;
        this.profileColorIndex = 0;
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.id = userId; // Synchroniser id avec userId
        this.username = username;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
        this.profileIconIndex = 0;
        this.profileColorIndex = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.id = userId; // Synchroniser id avec userId
    }
    
    // Getters et setters pour le champ id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
        this.userId = id; // Synchroniser userId avec id
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
    
    // Getters and setters for profile icon and color
    public int getProfileIconIndex() {
        return profileIconIndex;
    }
    
    public void setProfileIconIndex(int profileIconIndex) {
        this.profileIconIndex = profileIconIndex;
    }
    
    public int getProfileColorIndex() {
        return profileColorIndex;
    }
    
    public void setProfileColorIndex(int profileColorIndex) {
        this.profileColorIndex = profileColorIndex;
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
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                ", profileIconIndex=" + profileIconIndex +
                ", profileColorIndex=" + profileColorIndex +
                ", followersCount=" + followersCount +
                ", followingCount=" + followingCount +
                '}';
    }
} 