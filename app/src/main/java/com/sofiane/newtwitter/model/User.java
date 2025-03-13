package com.sofiane.newtwitter.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant un utilisateur dans l'application.
 * Cette classe contient toutes les informations relatives à un utilisateur,
 * y compris ses données de profil, ses relations de suivi et ses statistiques.
 */
public class User {
    private String userId;      // Identifiant unique de l'utilisateur
    private String id;          // Champ id pour la compatibilité avec Firebase (synchronisé avec userId)
    private String username;    // Nom d'utilisateur affiché
    private String email;       // Adresse email de l'utilisateur
    private String profileImageUrl; // URL de l'image de profil
    private String bannerImageUrl;  // URL de l'image de bannière
    private String bio;         // Biographie/description de l'utilisateur
    private long createdAt;     // Date de création du compte (timestamp)
    private Map<String, Boolean> followers; // Map des utilisateurs qui suivent cet utilisateur
    private Map<String, Boolean> following; // Map des utilisateurs que cet utilisateur suit
    private int followersCount; // Nombre de followers
    private int followingCount; // Nombre de suivis
    private int profileIconIndex;    // Index de l'icône de profil sélectionnée
    private int profileColorIndex;   // Index de la couleur de profil sélectionnée

    /**
     * Constructeur par défaut requis pour Firebase.
     * Initialise les collections et les valeurs par défaut.
     */
    public User() {
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.followersCount = 0;
        this.followingCount = 0;
        this.profileIconIndex = 0;   // Icône par défaut
        this.profileColorIndex = 0;  // Couleur par défaut
    }

    /**
     * Constructeur avec email uniquement.
     * Utilisé lors de la création d'un nouvel utilisateur avec des informations minimales.
     *
     * @param email L'adresse email de l'utilisateur
     */
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

    /**
     * Constructeur complet pour la création d'un utilisateur.
     *
     * @param userId   L'identifiant unique de l'utilisateur
     * @param username Le nom d'utilisateur
     * @param email    L'adresse email de l'utilisateur
     */
    public User(String userId, String username, String email) {
        this.userId = userId;
        this.id = userId; // Synchroniser id avec userId pour la compatibilité Firebase
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

    /**
     * Récupère l'identifiant unique de l'utilisateur.
     *
     * @return L'identifiant de l'utilisateur
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Définit l'identifiant unique de l'utilisateur.
     * Met également à jour le champ id pour maintenir la synchronisation.
     *
     * @param userId Le nouvel identifiant de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
        this.id = userId; // Synchroniser id avec userId
    }
    
    /**
     * Récupère le champ id de l'utilisateur (utilisé pour la compatibilité Firebase).
     * Ce champ est synchronisé avec userId.
     *
     * @return L'identifiant de l'utilisateur
     */
    public String getId() {
        return id;
    }
    
    /**
     * Définit le champ id de l'utilisateur.
     * Met également à jour le champ userId pour maintenir la synchronisation.
     *
     * @param id Le nouvel identifiant
     */
    public void setId(String id) {
        this.id = id;
        this.userId = id; // Synchroniser userId avec id
    }

    /**
     * Récupère le nom d'utilisateur.
     *
     * @return Le nom d'utilisateur
     */
    public String getUsername() {
        return username;
    }

    /**
     * Définit le nom d'utilisateur.
     *
     * @param username Le nouveau nom d'utilisateur
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Récupère l'adresse email de l'utilisateur.
     *
     * @return L'adresse email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Définit l'adresse email de l'utilisateur.
     *
     * @param email La nouvelle adresse email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Récupère l'URL de l'image de profil de l'utilisateur.
     *
     * @return L'URL de l'image de profil
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Définit l'URL de l'image de profil de l'utilisateur.
     *
     * @param profileImageUrl La nouvelle URL de l'image de profil
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    /**
     * Récupère l'URL de l'image de bannière de l'utilisateur.
     *
     * @return L'URL de l'image de bannière
     */
    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    /**
     * Définit l'URL de l'image de bannière de l'utilisateur.
     *
     * @param bannerImageUrl La nouvelle URL de l'image de bannière
     */
    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    /**
     * Récupère la biographie de l'utilisateur.
     *
     * @return La biographie
     */
    public String getBio() {
        return bio;
    }

    /**
     * Définit la biographie de l'utilisateur.
     *
     * @param bio La nouvelle biographie
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Récupère la date de création du compte (timestamp).
     *
     * @return La date de création en millisecondes
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Définit la date de création du compte.
     *
     * @param createdAt La date de création en millisecondes
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Récupère la liste des utilisateurs qui suivent cet utilisateur.
     * Initialise la map si elle est null.
     *
     * @return Une map des followers (clé: userId, valeur: true)
     */
    public Map<String, Boolean> getFollowers() {
        if (followers == null) {
            followers = new HashMap<>();
        }
        return followers;
    }

    /**
     * Définit la liste des utilisateurs qui suivent cet utilisateur.
     * Met également à jour le compteur de followers.
     *
     * @param followers La nouvelle map des followers
     */
    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
        this.followersCount = followers != null ? followers.size() : 0;
    }

    /**
     * Récupère la liste des utilisateurs que cet utilisateur suit.
     * Initialise la map si elle est null.
     *
     * @return Une map des utilisateurs suivis (clé: userId, valeur: true)
     */
    public Map<String, Boolean> getFollowing() {
        if (following == null) {
            following = new HashMap<>();
        }
        return following;
    }

    /**
     * Définit la liste des utilisateurs que cet utilisateur suit.
     * Met également à jour le compteur de following.
     *
     * @param following La nouvelle map des utilisateurs suivis
     */
    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
        this.followingCount = following != null ? following.size() : 0;
    }
    
    /**
     * Récupère le nombre de followers de l'utilisateur.
     *
     * @return Le nombre de followers
     */
    public int getFollowersCount() {
        return followersCount;
    }
    
    /**
     * Définit le nombre de followers de l'utilisateur.
     *
     * @param followersCount Le nouveau nombre de followers
     */
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }
    
    /**
     * Récupère le nombre d'utilisateurs que cet utilisateur suit.
     *
     * @return Le nombre d'utilisateurs suivis
     */
    public int getFollowingCount() {
        return followingCount;
    }
    
    /**
     * Définit le nombre d'utilisateurs que cet utilisateur suit.
     *
     * @param followingCount Le nouveau nombre d'utilisateurs suivis
     */
    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
    
    /**
     * Récupère l'index de l'icône de profil sélectionnée.
     *
     * @return L'index de l'icône de profil
     */
    public int getProfileIconIndex() {
        return profileIconIndex;
    }
    
    /**
     * Définit l'index de l'icône de profil.
     *
     * @param profileIconIndex Le nouvel index d'icône
     */
    public void setProfileIconIndex(int profileIconIndex) {
        this.profileIconIndex = profileIconIndex;
    }
    
    /**
     * Récupère l'index de la couleur de profil sélectionnée.
     *
     * @return L'index de la couleur de profil
     */
    public int getProfileColorIndex() {
        return profileColorIndex;
    }
    
    /**
     * Définit l'index de la couleur de profil.
     *
     * @param profileColorIndex Le nouvel index de couleur
     */
    public void setProfileColorIndex(int profileColorIndex) {
        this.profileColorIndex = profileColorIndex;
    }
    
    /**
     * Vérifie si cet utilisateur suit un autre utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur à vérifier
     * @return true si cet utilisateur suit l'utilisateur spécifié, false sinon
     */
    public boolean isFollowing(String userId) {
        return getFollowing().containsKey(userId);
    }
    
    /**
     * Vérifie si cet utilisateur est suivi par un autre utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur à vérifier
     * @return true si cet utilisateur est suivi par l'utilisateur spécifié, false sinon
     */
    public boolean isFollowedBy(String userId) {
        return getFollowers().containsKey(userId);
    }
    
    /**
     * Ajoute un follower à cet utilisateur.
     * Met également à jour le compteur de followers.
     *
     * @param userId L'identifiant de l'utilisateur à ajouter comme follower
     */
    public void addFollower(String userId) {
        getFollowers().put(userId, true);
        this.followersCount = getFollowers().size();
    }
    
    /**
     * Supprime un follower de cet utilisateur.
     * Met également à jour le compteur de followers.
     *
     * @param userId L'identifiant de l'utilisateur à supprimer des followers
     */
    public void removeFollower(String userId) {
        getFollowers().remove(userId);
        this.followersCount = getFollowers().size();
    }
    
    /**
     * Ajoute un utilisateur à la liste des utilisateurs suivis.
     * Met également à jour le compteur de following.
     *
     * @param userId L'identifiant de l'utilisateur à suivre
     */
    public void addFollowing(String userId) {
        getFollowing().put(userId, true);
        this.followingCount = getFollowing().size();
    }
    
    /**
     * Supprime un utilisateur de la liste des utilisateurs suivis.
     * Met également à jour le compteur de following.
     *
     * @param userId L'identifiant de l'utilisateur à ne plus suivre
     */
    public void removeFollowing(String userId) {
        getFollowing().remove(userId);
        this.followingCount = getFollowing().size();
    }
    
    /**
     * Retourne une représentation textuelle de l'objet User.
     * Utile pour le débogage et la journalisation.
     *
     * @return Une chaîne de caractères représentant l'utilisateur et ses attributs
     */
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", id='" + id + '\'' +
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