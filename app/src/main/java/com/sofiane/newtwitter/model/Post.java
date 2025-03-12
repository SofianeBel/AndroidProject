package com.sofiane.newtwitter.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.PropertyName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post {
    private String id;
    private String userId;
    private String username;
    private String content;
    private String imageUrl;
    @PropertyName("createdAt")
    private Date createdAt;
    private int likeCount;
    private int commentCount;
    // Nouveaux champs pour les réponses et les retweets
    private int retweetCount;
    private String parentId; // ID du post parent (pour les réponses)
    private String parentUsername; // Nom d'utilisateur du post parent (pour les réponses)
    private String originalPostId; // ID du post original (pour les retweets)
    private String originalUserId; // ID de l'utilisateur original (pour les retweets)
    private String originalUsername; // Nom de l'utilisateur original (pour les retweets)
    private boolean isRetweet;
    private boolean isReply;

    // Required empty constructor for Firebase
    public Post() {
        // Firebase requires an empty constructor
        this.createdAt = new Date();
        this.retweetCount = 0;
        this.isRetweet = false;
        this.isReply = false;
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
        this.retweetCount = 0;
        this.isRetweet = false;
        this.isReply = false;
    }

    // Constructeur pour les réponses
    public Post(String id, String userId, String username, String content, String imageUrl, Date createdAt, int likeCount, String parentId) {
        this(id, userId, username, content, imageUrl, createdAt, likeCount);
        this.parentId = parentId;
        this.isReply = true;
    }

    // Constructeur pour les réponses avec nom d'utilisateur parent
    public Post(String id, String userId, String username, String content, String imageUrl, Date createdAt, int likeCount, String parentId, String parentUsername) {
        this(id, userId, username, content, imageUrl, createdAt, likeCount);
        this.parentId = parentId;
        this.parentUsername = parentUsername;
        this.isReply = true;
    }

    // Constructeur pour les retweets
    public Post(String id, String userId, String username, String originalPostId, String originalUserId, String originalUsername, String content, String imageUrl, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.originalPostId = originalPostId;
        this.originalUserId = originalUserId;
        this.originalUsername = originalUsername;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.likeCount = 0;
        this.commentCount = 0;
        this.retweetCount = 0;
        this.isRetweet = true;
        this.isReply = false;
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

    @PropertyName("createdAt")
    public Date getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Méthode pour gérer la conversion de timestamp en Date
    @Exclude
    public void setCreatedAtFromTimestamp(Object timestamp) {
        if (timestamp instanceof Long) {
            this.createdAt = new Date((Long) timestamp);
        } else if (timestamp instanceof Map) {
            // Ignorer les autres formats
            this.createdAt = new Date();
        }
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
    
    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
        this.isReply = parentId != null && !parentId.isEmpty();
    }

    public String getParentUsername() {
        return parentUsername;
    }
    
    public void setParentUsername(String parentUsername) {
        this.parentUsername = parentUsername;
    }

    public String getOriginalPostId() {
        return originalPostId;
    }

    public void setOriginalPostId(String originalPostId) {
        this.originalPostId = originalPostId;
    }

    public String getOriginalUserId() {
        return originalUserId;
    }

    public void setOriginalUserId(String originalUserId) {
        this.originalUserId = originalUserId;
    }

    public String getOriginalUsername() {
        return originalUsername;
    }

    public void setOriginalUsername(String originalUsername) {
        this.originalUsername = originalUsername;
    }

    public boolean isRetweet() {
        return isRetweet;
    }

    public void setRetweet(boolean retweet) {
        isRetweet = retweet;
    }

    // Ajout des méthodes pour la compatibilité Firebase
    @Exclude
    public Boolean getRetweet() {
        return isRetweet;
    }

    @Exclude
    public void setRetweet(Boolean retweet) {
        if (retweet != null) {
            this.isRetweet = retweet;
        }
    }

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean reply) {
        isReply = reply;
    }
    
    // Ajout des méthodes pour la compatibilité Firebase
    @Exclude
    public Boolean getReply() {
        return isReply;
    }

    @Exclude
    public void setReply(Boolean reply) {
        if (reply != null) {
            this.isReply = reply;
        }
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
        result.put("retweetCount", retweetCount);
        result.put("isRetweet", isRetweet);
        result.put("isReply", isReply);
        
        if (parentId != null) {
            result.put("parentId", parentId);
        }
        
        if (parentUsername != null) {
            result.put("parentUsername", parentUsername);
        }
        
        if (isRetweet) {
            result.put("originalPostId", originalPostId);
            result.put("originalUserId", originalUserId);
            result.put("originalUsername", originalUsername);
        }
        
        return result;
    }
} 