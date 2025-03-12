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

public class PostRepository {
    private static final String TAG = "PostRepository";
    private static PostRepository instance;
    
    // Flag to track if sample posts have been created
    private boolean samplePostsCreated = false;
    
    // Firebase references
    private final DatabaseReference postsRef;
    private final DatabaseReference likesRef;
    
    // LiveData
    private final MutableLiveData<List<Post>> allPostsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private PostRepository() {
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        postsRef = database.getReference("posts");
        likesRef = database.getReference("likes");
        
        // Load initial data
        loadAllPosts();
    }

    public static PostRepository getInstance() {
        if (instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    // LiveData getters
    public LiveData<List<Post>> getAllPostsLiveData() {
        return allPostsLiveData;
    }
    
    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    // Load all posts from Firebase
    public void loadAllPosts() {
        try {
            Log.d(TAG, "Starting to load all posts from Firebase");
            // Query to get all posts ordered by creation time (newest first)
            // Firebase ne supporte pas directement le tri décroissant, nous devons donc
            // trier côté client ou utiliser une autre approche
            Query query = postsRef;
            
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Post> posts = new ArrayList<>();
                        Log.d(TAG, "onDataChange called, snapshot has " + dataSnapshot.getChildrenCount() + " children");
                        
                        // Loop through all posts
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Post post = postSnapshot.getValue(Post.class);
                                if (post != null) {
                                    posts.add(post); // Add to list
                                    Log.d(TAG, "Loaded post: " + post.getId() + ", content: " + post.getContent());
                                } else {
                                    Log.w(TAG, "Failed to parse post from: " + postSnapshot.getKey());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing post: " + e.getMessage(), e);
                            }
                        }
                        
                        // Trier les posts par date (du plus récent au plus ancien)
                        posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                        
                        // Toujours mettre à jour la LiveData, même si la liste est vide
                        allPostsLiveData.setValue(posts);
                        
                        // Log le résultat
                        if (posts.isEmpty()) {
                            Log.d(TAG, "No posts found in Firebase");
                        } else {
                            Log.d(TAG, "Loaded " + posts.size() + " posts from Firebase");
                        }
                        
                        // Si aucun post n'est trouvé, créer quelques posts de test
                        // Mais seulement si c'est la première fois
                        if (posts.isEmpty() && !samplePostsCreated) {
                            Log.d(TAG, "No posts found, creating sample posts");
                            samplePostsCreated = true;
                            createSamplePosts();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing posts: " + e.getMessage(), e);
                        errorMessageLiveData.setValue("Error loading posts: " + e.getMessage());
                        // Même en cas d'erreur, mettre à jour la LiveData avec une liste vide
                        allPostsLiveData.setValue(new ArrayList<>());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    errorMessageLiveData.setValue("Database error: " + databaseError.getMessage());
                    // En cas d'erreur, mettre à jour la LiveData avec une liste vide
                    allPostsLiveData.setValue(new ArrayList<>());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up posts listener: " + e.getMessage(), e);
            errorMessageLiveData.setValue("Error setting up posts listener: " + e.getMessage());
            // En cas d'erreur, mettre à jour la LiveData avec une liste vide
            allPostsLiveData.setValue(new ArrayList<>());
        }
    }
    
    // Créer des posts de test si aucun post n'est trouvé
    private void createSamplePosts() {
        try {
            Log.d(TAG, "Creating sample posts");
            // Créer quelques posts de test avec des délais pour éviter les conflits
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                createSamplePost("user1", "User1", "Bienvenue sur notre nouvelle application de réseau social!");
            }, 100);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                createSamplePost("user2", "User2", "J'adore cette nouvelle application! Tellement simple à utiliser.");
            }, 300);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                createSamplePost("user3", "User3", "Bonjour à tous! Je viens de rejoindre cette plateforme.");
            }, 500);
            
            // Ne pas rappeler loadAllPosts() pour éviter une boucle infinie
            // Les posts seront chargés automatiquement grâce au ValueEventListener
        } catch (Exception e) {
            Log.e(TAG, "Error creating sample posts: " + e.getMessage(), e);
        }
    }
    
    // Méthode spécifique pour créer des posts de test
    private void createSamplePost(String userId, String username, String content) {
        try {
            // Generate a unique key for the new post
            String postId = postsRef.push().getKey();
            if (postId == null) {
                Log.e(TAG, "Failed to create post ID for sample post");
                return;
            }
            
            // Create post object with current timestamp
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
                    Log.d(TAG, "Sample post created successfully with ID: " + postId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating sample post: " + e.getMessage(), e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error creating sample post: " + e.getMessage(), e);
        }
    }

    // Create a new post in Firebase
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

    // Create a post with a specific username (for testing)
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

    // Get posts by a specific user
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

    // Like a post
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

    // Delete a post
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
} 