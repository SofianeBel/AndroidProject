package com.sofiane.newtwitter.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sofiane.newtwitter.model.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository pour gérer les données des posts (tweets).
 * Cette classe implémente le pattern Singleton pour assurer une instance unique.
 * Elle gère les opérations CRUD pour les posts, ainsi que les interactions comme
 * les likes, les retweets et les réponses.
 * Les données sont stockées dans Firebase Realtime Database.
 */
public class PostRepository {
    private static final String TAG = "PostRepository";
    private static PostRepository instance;
    
    // Firebase references
    private final DatabaseReference postsRef;
    private final DatabaseReference likesRef;
    private final DatabaseReference retweetsRef;
    
    // LiveData
    private final MutableLiveData<List<Post>> allPostsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * Initialise les références Firebase nécessaires.
     */
    private PostRepository() {
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        postsRef = database.getReference("posts");
        likesRef = database.getReference("likes");
        retweetsRef = database.getReference("retweets");
        
        // Load initial data
        loadAllPosts();
    }

    /**
     * Obtient l'instance unique du repository.
     * Crée une nouvelle instance si elle n'existe pas encore.
     *
     * @return L'instance unique de PostRepository
     */
    public static PostRepository getInstance() {
        if (instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    /**
     * Récupère le LiveData contenant tous les posts.
     *
     * @return LiveData contenant la liste des posts
     */
    public LiveData<List<Post>> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    /**
     * Récupère le LiveData contenant les messages d'erreur.
     *
     * @return LiveData contenant les messages d'erreur
     */
    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    /**
     * Charge tous les posts depuis Firebase et met à jour le LiveData.
     */
    public void loadAllPosts() {
        try {
            postsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Post> posts = new ArrayList<>();
                        
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            if (post != null) {
                                posts.add(post);
                            }
                        }
                        
                        // Sort by creation date (newest first)
                        posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                        
                        // Update LiveData
                        allPostsLiveData.setValue(posts);
                        Log.d(TAG, "Loaded " + posts.size() + " posts");
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing posts: " + e.getMessage(), e);
                        errorMessageLiveData.setValue("Error parsing posts: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Database error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up posts listener: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error setting up posts listener: " + e.getMessage());
            // En cas d'erreur, mettre à jour la LiveData avec une liste vide
            allPostsLiveData.setValue(new ArrayList<>());
        }
    }

    /**
     * Crée un nouveau post pour l'utilisateur actuellement connecté.
     *
     * @param content Le contenu du post
     */
    public void createPost(String content) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                errorMessageLiveData.setValue("You must be logged in to post");
                return;
            }
            
            String userId = currentUser.getUid();
            String username = currentUser.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = "User" + userId.substring(0, 5);
            }
            
            // Generate a unique key for the new post
            String postId = postsRef.push().getKey();
            if (postId == null) {
                errorMessageLiveData.setValue("Failed to create post ID");
                return;
            }
            
            // Create post object
            Post post = new Post(
                postId,
                userId,
                username,
                content,
                null, // No image URL for now
                new Date(),
                0 // Initial like count
            );
            
            // Save post to Firebase
            postsRef.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post created successfully with ID: " + postId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post: " + e.getMessage(), e);
                    errorMessageLiveData.setValue("Failed to create post: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e(TAG, "Error creating post: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error creating post: " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau post pour un utilisateur spécifique.
     *
     * @param userId   L'identifiant de l'auteur du post
     * @param username Le nom d'utilisateur de l'auteur
     * @param content  Le contenu du post
     */
    public void createPost(String userId, String username, String content) {
        try {
            // Generate a unique key for the new post
            String postId = postsRef.push().getKey();
            if (postId == null) {
                errorMessageLiveData.setValue("Failed to create post ID");
                return;
            }
            
            // Create post object
            Post post = new Post(
                postId,
                userId,
                username,
                content,
                null, // No image URL for now
                new Date(),
                0 // Initial like count
            );
            
            // Save post to Firebase
            postsRef.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post created successfully with ID: " + postId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post: " + e.getMessage(), e);
                    errorMessageLiveData.setValue("Failed to create post: " + e.getMessage());
                });
        } catch (Exception e) {
            Log.e(TAG, "Error creating post: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error creating post: " + e.getMessage());
        }
    }

    /**
     * Charge les posts d'un utilisateur spécifique.
     *
     * @param userId L'identifiant de l'utilisateur
     */
    public void loadPostsByUser(String userId) {
        try {
            Query query = postsRef.orderByChild("userId").equalTo(userId);
            
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Post> userPosts = new ArrayList<>();
                        
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            if (post != null) {
                                userPosts.add(post);
                            }
                        }
                        
                        // Sort by creation date (newest first)
                        userPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                        
                        // We're not setting allPostsLiveData here as this is for a specific user view
                        Log.d(TAG, "Loaded " + userPosts.size() + " posts for user " + userId);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user posts: " + e.getMessage(), e);
                        errorMessageLiveData.setValue("Error loading user posts: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Database error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up user posts listener: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error setting up user posts listener: " + e.getMessage());
        }
    }

    /**
     * Ajoute ou supprime un like sur un post.
     * Met à jour le compteur de likes du post.
     *
     * @param postId L'identifiant du post à liker/unliker
     */
    public void likePost(String postId) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                errorMessageLiveData.setValue("You must be logged in to like posts");
                return;
            }
            
            String userId = currentUser.getUid();
            String likeKey = postId + "_" + userId;
            
            // Check if user already liked this post
            likesRef.child(likeKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User already liked this post, unlike it
                        likesRef.child(likeKey).removeValue();
                        
                        // Decrement like count
                        postsRef.child(postId).child("likeCount").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Integer currentLikes = snapshot.getValue(Integer.class);
                                    if (currentLikes != null && currentLikes > 0) {
                                        postsRef.child(postId).child("likeCount").setValue(currentLikes - 1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error updating like count: " + error.getMessage());
                                }
                            });
                    } else {
                        // User hasn't liked this post yet, like it
                        likesRef.child(likeKey).setValue(true);
                        
                        // Increment like count
                        postsRef.child(postId).child("likeCount").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Integer currentLikes = snapshot.getValue(Integer.class);
                                    if (currentLikes != null) {
                                        postsRef.child(postId).child("likeCount").setValue(currentLikes + 1);
                                    } else {
                                        postsRef.child(postId).child("likeCount").setValue(1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error updating like count: " + error.getMessage());
                                }
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Database error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error liking post: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error liking post: " + e.getMessage());
        }
    }

    /**
     * Supprime un post et ses références associées.
     *
     * @param postId L'identifiant du post à supprimer
     */
    public void deletePost(String postId) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                errorMessageLiveData.setValue("You must be logged in to delete posts");
                return;
            }
            
            // Check if the current user is the author of the post
            postsRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Post post = dataSnapshot.getValue(Post.class);
                    
                    if (post != null && post.getUserId().equals(currentUser.getUid())) {
                        // User is the author, delete the post
                        postsRef.child(postId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Post deleted successfully: " + postId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting post: " + e.getMessage(), e);
                                errorMessageLiveData.setValue("Failed to delete post: " + e.getMessage());
                            });
                    } else {
                        // User is not the author
                        errorMessageLiveData.setValue("You can only delete your own posts");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Database error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting post: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error deleting post: " + e.getMessage());
        }
    }

    /**
     * Crée une réponse à un post existant.
     *
     * @param content      Le contenu de la réponse
     * @param parentPostId L'identifiant du post parent
     */
    public void createReply(String content, String parentPostId) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                errorMessageLiveData.setValue("You must be logged in to reply");
                return;
            }
            
            String userId = currentUser.getUid();
            String username = currentUser.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = "User" + userId.substring(0, 5);
            }
            
            // Créer des copies finales des variables pour utilisation dans la classe anonyme
            final String finalUserId = userId;
            final String finalUsername = username;
            final String finalContent = content;
            final String finalParentPostId = parentPostId;
            
            // Generate a unique key for the new reply
            String replyId = postsRef.push().getKey();
            if (replyId == null) {
                errorMessageLiveData.setValue("Failed to create reply ID");
                return;
            }
            
            final String finalReplyId = replyId;
            
            // Récupérer le post parent pour obtenir le nom d'utilisateur
            postsRef.child(finalParentPostId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Post parentPost = dataSnapshot.getValue(Post.class);
                        String parentUsername = parentPost != null ? parentPost.getUsername() : "";
                        
                        // Create reply object
                        Post reply = new Post(
                            finalReplyId,
                            finalUserId,
                            finalUsername,
                            finalContent,
                            null, // No image URL for now
                            new Date(),
                            0, // Initial like count
                            finalParentPostId, // Parent post ID
                            parentUsername // Parent username
                        );
                        
                        // Save reply to Firebase
                        postsRef.child(finalReplyId).setValue(reply)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Reply created successfully with ID: " + finalReplyId);
                                
                                // Increment comment count on parent post
                                postsRef.child(finalParentPostId).child("commentCount").addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Integer currentComments = snapshot.getValue(Integer.class);
                                            if (currentComments != null) {
                                                postsRef.child(finalParentPostId).child("commentCount").setValue(currentComments + 1);
                                            } else {
                                                postsRef.child(finalParentPostId).child("commentCount").setValue(1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Error updating comment count: " + error.getMessage());
                                        }
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating reply: " + e.getMessage(), e);
                                errorMessageLiveData.setValue("Failed to create reply: " + e.getMessage());
                            });
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating reply: " + e.getMessage(), e);
                        errorMessageLiveData.setValue("Error creating reply: " + e.getMessage());
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error retrieving parent post: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Error retrieving parent post: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating reply: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error creating reply: " + e.getMessage());
        }
    }

    /**
     * Crée un retweet d'un post existant.
     *
     * @param originalPost Le post original à retweeter
     */
    public void retweetPost(Post originalPost) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                errorMessageLiveData.setValue("You must be logged in to retweet");
                return;
            }
            
            final String userId = currentUser.getUid();
            final String username;
            if (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()) {
                username = "User" + userId.substring(0, 5);
            } else {
                username = currentUser.getDisplayName();
            }
            
            // Vérifier si l'utilisateur a déjà retweeté ce post
            String retweetKey = originalPost.getId() + "_" + userId + "_retweet";
            retweetsRef.child(retweetKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // L'utilisateur a déjà retweeté ce post, annuler le retweet
                        retweetsRef.child(retweetKey).removeValue();
                        
                        // Supprimer le retweet
                        String retweetId = dataSnapshot.getValue(String.class);
                        if (retweetId != null) {
                            postsRef.child(retweetId).removeValue();
                        }
                        
                        // Décrémenter le compteur de retweets
                        postsRef.child(originalPost.getId()).child("retweetCount").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Integer currentRetweets = snapshot.getValue(Integer.class);
                                    if (currentRetweets != null && currentRetweets > 0) {
                                        postsRef.child(originalPost.getId()).child("retweetCount").setValue(currentRetweets - 1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error updating retweet count: " + error.getMessage());
                                }
                            });
                    } else {
                        // L'utilisateur n'a pas encore retweeté ce post, créer un retweet
                        
                        // Generate a unique key for the new retweet
                        String retweetId = postsRef.push().getKey();
                        if (retweetId == null) {
                            errorMessageLiveData.setValue("Failed to create retweet ID");
                            return;
                        }
                        
                        // Create retweet object
                        Post retweet = new Post(
                            retweetId,
                            userId,
                            username,
                            originalPost.getId(),
                            originalPost.getUserId(),
                            originalPost.getUsername(),
                            originalPost.getContent(),
                            originalPost.getImageUrl(),
                            new Date()
                        );
                        
                        // Save retweet to Firebase
                        postsRef.child(retweetId).setValue(retweet)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Retweet created successfully with ID: " + retweetId);
                                
                                // Enregistrer la référence du retweet
                                retweetsRef.child(retweetKey).setValue(retweetId);
                                
                                // Incrémenter le compteur de retweets
                                postsRef.child(originalPost.getId()).child("retweetCount").addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Integer currentRetweets = snapshot.getValue(Integer.class);
                                            if (currentRetweets != null) {
                                                postsRef.child(originalPost.getId()).child("retweetCount").setValue(currentRetweets + 1);
                                            } else {
                                                postsRef.child(originalPost.getId()).child("retweetCount").setValue(1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Error updating retweet count: " + error.getMessage());
                                        }
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating retweet: " + e.getMessage(), e);
                                errorMessageLiveData.setValue("Failed to create retweet: " + e.getMessage());
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Database error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error retweeting post: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error retweeting post: " + e.getMessage());
        }
    }

    /**
     * Charge les retweets et les réponses pour les afficher dans le fil d'actualité.
     * Met à jour allPostsLiveData avec les résultats.
     */
    public void loadRetweetsAndReplies() {
        try {
            Log.d(TAG, "Starting to load retweets and replies from Firebase");
            Query query = postsRef;
            
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Post> posts = new ArrayList<>();
                        Log.d(TAG, "onDataChange called for retweets and replies, snapshot has " + dataSnapshot.getChildrenCount() + " children");
                        
                        // Loop through all posts
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Post post = postSnapshot.getValue(Post.class);
                                if (post != null) {
                                    // Ne garder que les retweets et les réponses
                                    if (post.isRetweet() || post.isReply()) {
                                        posts.add(post);
                                        Log.d(TAG, "Loaded retweet/reply: " + post.getId() + ", content: " + post.getContent());
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing retweet/reply: " + e.getMessage(), e);
                            }
                        }
                        
                        // Trier les posts par date (du plus récent au plus ancien)
                        posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                        
                        // Mettre à jour une LiveData spécifique pour les retweets et réponses si nécessaire
                        // Pour l'instant, nous n'avons pas créé cette LiveData, mais on pourrait l'ajouter
                        // retweetsAndRepliesLiveData.setValue(posts);
                        
                        Log.d(TAG, "Loaded " + posts.size() + " retweets and replies from Firebase");
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing retweets and replies: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading retweets and replies: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Error loading retweets and replies: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up retweets and replies listener: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error setting up retweets and replies listener: " + e.getMessage());
        }
    }

    /**
     * Charge les réponses à un post spécifique.
     * Met à jour allPostsLiveData avec les résultats.
     *
     * @param parentPostId L'identifiant du post parent
     */
    public void loadRepliesForPost(String parentPostId) {
        try {
            Log.d(TAG, "Starting to load replies for post: " + parentPostId);
            Query query = postsRef.orderByChild("parentId").equalTo(parentPostId);
            
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Post> replies = new ArrayList<>();
                        Log.d(TAG, "onDataChange called for replies, snapshot has " + dataSnapshot.getChildrenCount() + " children");
                        
                        // Loop through all replies
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Post post = postSnapshot.getValue(Post.class);
                                if (post != null && post.isReply() && parentPostId.equals(post.getParentId())) {
                                    replies.add(post);
                                    Log.d(TAG, "Loaded reply: " + post.getId() + ", content: " + post.getContent());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing reply: " + e.getMessage(), e);
                            }
                        }
                        
                        // Trier les réponses par date (du plus récent au plus ancien)
                        replies.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                        
                        // Mettre à jour une LiveData spécifique pour les réponses
                        // Pour l'instant, nous n'avons pas créé cette LiveData, mais on pourrait l'ajouter
                        // repliesLiveData.setValue(replies);
                        
                        Log.d(TAG, "Loaded " + replies.size() + " replies for post " + parentPostId);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing replies: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading replies: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Error loading replies: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up replies listener: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error setting up replies listener: " + e.getMessage());
        }
    }
} 