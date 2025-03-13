package com.sofiane.newtwitter.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.PropertyName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant un post (tweet) dans l'application.
 * Cette classe gère les posts originaux, les réponses et les retweets.
 * Elle contient toutes les informations relatives à un post, y compris
 * son contenu, son auteur, et ses statistiques d'interaction.
 */
public class Post {
    private String id;                // Identifiant unique du post
    private String userId;            // Identifiant de l'auteur du post
    private String username;          // Nom d'utilisateur de l'auteur
    private String content;           // Contenu textuel du post
    private String imageUrl;          // URL de l'image attachée au post (optionnel)
    @PropertyName("createdAt")
    private Date createdAt;           // Date de création du post
    private int likeCount;            // Nombre de likes
    private int commentCount;         // Nombre de commentaires
    // Nouveaux champs pour les réponses et les retweets
    private int retweetCount;         // Nombre de retweets
    private String parentId;          // ID du post parent (pour les réponses)
    private String parentUsername;    // Nom d'utilisateur du post parent (pour les réponses)
    private String originalPostId;    // ID du post original (pour les retweets)
    private String originalUserId;    // ID de l'utilisateur original (pour les retweets)
    private String originalUsername;  // Nom de l'utilisateur original (pour les retweets)
    private boolean isRetweet;        // Indique si ce post est un retweet
    private boolean isReply;          // Indique si ce post est une réponse

    /**
     * Constructeur par défaut requis pour Firebase.
     * Initialise les valeurs par défaut.
     */
    public Post() {
        this.createdAt = new Date();
        this.retweetCount = 0;
        this.isRetweet = false;
        this.isReply = false;
    }

    /**
     * Constructeur pour un post standard.
     *
     * @param id         Identifiant unique du post
     * @param userId     Identifiant de l'auteur
     * @param username   Nom d'utilisateur de l'auteur
     * @param content    Contenu textuel du post
     * @param imageUrl   URL de l'image attachée (peut être null)
     * @param createdAt  Date de création du post
     * @param likeCount  Nombre initial de likes
     */
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

    /**
     * Constructeur pour un post qui est une réponse à un autre post.
     *
     * @param id         Identifiant unique du post
     * @param userId     Identifiant de l'auteur
     * @param username   Nom d'utilisateur de l'auteur
     * @param content    Contenu textuel du post
     * @param imageUrl   URL de l'image attachée (peut être null)
     * @param createdAt  Date de création du post
     * @param likeCount  Nombre initial de likes
     * @param parentId   Identifiant du post parent auquel celui-ci répond
     */
    public Post(String id, String userId, String username, String content, String imageUrl, Date createdAt, int likeCount, String parentId) {
        this(id, userId, username, content, imageUrl, createdAt, likeCount);
        this.parentId = parentId;
        this.isReply = true;
    }

    /**
     * Constructeur pour un post qui est une réponse à un autre post, avec le nom d'utilisateur du parent.
     *
     * @param id             Identifiant unique du post
     * @param userId         Identifiant de l'auteur
     * @param username       Nom d'utilisateur de l'auteur
     * @param content        Contenu textuel du post
     * @param imageUrl       URL de l'image attachée (peut être null)
     * @param createdAt      Date de création du post
     * @param likeCount      Nombre initial de likes
     * @param parentId       Identifiant du post parent auquel celui-ci répond
     * @param parentUsername Nom d'utilisateur de l'auteur du post parent
     */
    public Post(String id, String userId, String username, String content, String imageUrl, Date createdAt, int likeCount, String parentId, String parentUsername) {
        this(id, userId, username, content, imageUrl, createdAt, likeCount);
        this.parentId = parentId;
        this.parentUsername = parentUsername;
        this.isReply = true;
    }

    /**
     * Constructeur pour un post qui est un retweet d'un autre post.
     *
     * @param id                Identifiant unique du post
     * @param userId            Identifiant de l'auteur du retweet
     * @param username          Nom d'utilisateur de l'auteur du retweet
     * @param originalPostId    Identifiant du post original retweeté
     * @param originalUserId    Identifiant de l'auteur du post original
     * @param originalUsername  Nom d'utilisateur de l'auteur du post original
     * @param content           Contenu textuel du post original
     * @param imageUrl          URL de l'image attachée au post original (peut être null)
     * @param createdAt         Date de création du retweet
     */
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

    /**
     * Génère une chaîne de caractères représentant le temps écoulé depuis la création du post.
     * Par exemple : "2 heures", "5 minutes", "à l'instant", etc.
     * Cette méthode est exclue de la sérialisation Firebase.
     *
     * @return Une chaîne de caractères représentant le temps relatif
     */
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
        
        if (diffInSeconds < 60) {
            return "just now";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + (diffInMinutes == 1 ? " minute" : " minutes");
        } else if (diffInHours < 24) {
            return diffInHours + (diffInHours == 1 ? " hour" : " hours");
        } else if (diffInDays < 7) {
            return diffInDays + (diffInDays == 1 ? " day" : " days");
        } else {
            return diffInDays / 7 + ((diffInDays / 7) == 1 ? " week" : " weeks");
        }
    }

    // Getters and setters
    /**
     * Récupère l'identifiant unique du post.
     *
     * @return L'identifiant du post
     */
    public String getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique du post.
     *
     * @param id Le nouvel identifiant du post
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Récupère l'identifiant de l'auteur du post.
     *
     * @return L'identifiant de l'auteur
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Définit l'identifiant de l'auteur du post.
     *
     * @param userId Le nouvel identifiant de l'auteur
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Récupère le nom d'utilisateur de l'auteur du post.
     *
     * @return Le nom d'utilisateur de l'auteur
     */
    public String getUsername() {
        return username;
    }

    /**
     * Définit le nom d'utilisateur de l'auteur du post.
     *
     * @param username Le nouveau nom d'utilisateur de l'auteur
     */
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
            Map<String, Object> timestampMap = (Map<String, Object>) timestamp;
            if (timestampMap.containsKey("time")) {
                Object timeValue = timestampMap.get("time");
                if (timeValue instanceof Long) {
                    this.createdAt = new Date((Long) timeValue);
                    return;
                }
            }
            // Si on ne peut pas extraire le temps, utiliser la date actuelle
            this.createdAt = new Date();
        } else if (timestamp instanceof HashMap) {
            HashMap<String, Object> timestampMap = (HashMap<String, Object>) timestamp;
            if (timestampMap.containsKey("time")) {
                Object timeValue = timestampMap.get("time");
                if (timeValue instanceof Long) {
                    this.createdAt = new Date((Long) timeValue);
                    return;
                }
            }
            // Si on ne peut pas extraire le temps, utiliser la date actuelle
            this.createdAt = new Date();
        } else {
            // Pour tout autre type, utiliser la date actuelle
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