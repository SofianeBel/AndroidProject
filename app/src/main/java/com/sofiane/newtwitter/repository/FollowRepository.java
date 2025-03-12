package com.sofiane.newtwitter.repository;

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
import com.google.firebase.database.ValueEventListener;
import com.sofiane.newtwitter.model.User;

import java.util.HashMap;
import java.util.Map;

public class FollowRepository {
    private static final String TAG = "FollowRepository";
    private static FollowRepository instance;
    
    // Firebase references
    private final DatabaseReference usersRef;
    private final DatabaseReference followsRef;
    
    // LiveData
    private final MutableLiveData<Boolean> followStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> followersCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> followingCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private FollowRepository() {
        // Initialize Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");
        followsRef = database.getReference("follows");
    }

    public static FollowRepository getInstance() {
        if (instance == null) {
            instance = new FollowRepository();
        }
        return instance;
    }

    // LiveData getters
    public LiveData<Boolean> getFollowStatusLiveData() {
        return followStatusLiveData;
    }
    
    public LiveData<Integer> getFollowersCountLiveData() {
        return followersCountLiveData;
    }
    
    public LiveData<Integer> getFollowingCountLiveData() {
        return followingCountLiveData;
    }
    
    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    // Check if current user is following a specific user
    public void checkFollowStatus(String targetUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            followStatusLiveData.setValue(false);
            return;
        }
        
        String currentUserId = currentUser.getUid();
        
        // Check in the follows node
        followsRef.child(currentUserId).child("following").child(targetUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isFollowing = snapshot.exists();
                        followStatusLiveData.setValue(isFollowing);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking follow status: " + error.getMessage());
                        errorMessageLiveData.setValue("Error checking follow status: " + error.getMessage());
                        followStatusLiveData.setValue(false);
                    }
                });
    }

    // Follow a user
    public void followUser(String targetUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to follow users");
            return;
        }
        
        String currentUserId = currentUser.getUid();
        
        // Don't allow self-follow
        if (currentUserId.equals(targetUserId)) {
            errorMessageLiveData.setValue("You cannot follow yourself");
            return;
        }
        
        // Update follows node
        Map<String, Object> followUpdates = new HashMap<>();
        followUpdates.put("/follows/" + currentUserId + "/following/" + targetUserId, true);
        followUpdates.put("/follows/" + targetUserId + "/followers/" + currentUserId, true);
        
        // Update user nodes
        followUpdates.put("/users/" + currentUserId + "/following/" + targetUserId, true);
        followUpdates.put("/users/" + targetUserId + "/followers/" + currentUserId, true);
        
        // Update counts
        usersRef.child(currentUserId).child("followingCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    count = snapshot.getValue(Integer.class);
                }
                followUpdates.put("/users/" + currentUserId + "/followingCount", count + 1);
                
                usersRef.child(targetUserId).child("followersCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        if (snapshot.exists()) {
                            count = snapshot.getValue(Integer.class);
                        }
                        followUpdates.put("/users/" + targetUserId + "/followersCount", count + 1);
                        
                        // Apply all updates in a single transaction
                        FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference().updateChildren(followUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully followed user: " + targetUserId);
                                    followStatusLiveData.setValue(true);
                                    loadFollowCounts(targetUserId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error following user: " + e.getMessage(), e);
                                    errorMessageLiveData.setValue("Error following user: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting followers count: " + error.getMessage());
                        errorMessageLiveData.setValue("Error getting followers count: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting following count: " + error.getMessage());
                errorMessageLiveData.setValue("Error getting following count: " + error.getMessage());
            }
        });
    }

    // Unfollow a user
    public void unfollowUser(String targetUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            errorMessageLiveData.setValue("You must be logged in to unfollow users");
            return;
        }
        
        String currentUserId = currentUser.getUid();
        
        // Update follows node
        Map<String, Object> unfollowUpdates = new HashMap<>();
        unfollowUpdates.put("/follows/" + currentUserId + "/following/" + targetUserId, null);
        unfollowUpdates.put("/follows/" + targetUserId + "/followers/" + currentUserId, null);
        
        // Update user nodes
        unfollowUpdates.put("/users/" + currentUserId + "/following/" + targetUserId, null);
        unfollowUpdates.put("/users/" + targetUserId + "/followers/" + currentUserId, null);
        
        // Update counts
        usersRef.child(currentUserId).child("followingCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    count = snapshot.getValue(Integer.class);
                }
                if (count > 0) {
                    unfollowUpdates.put("/users/" + currentUserId + "/followingCount", count - 1);
                }
                
                usersRef.child(targetUserId).child("followersCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        if (snapshot.exists()) {
                            count = snapshot.getValue(Integer.class);
                        }
                        if (count > 0) {
                            unfollowUpdates.put("/users/" + targetUserId + "/followersCount", count - 1);
                        }
                        
                        // Apply all updates in a single transaction
                        FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference().updateChildren(unfollowUpdates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully unfollowed user: " + targetUserId);
                                    followStatusLiveData.setValue(false);
                                    loadFollowCounts(targetUserId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error unfollowing user: " + e.getMessage(), e);
                                    errorMessageLiveData.setValue("Error unfollowing user: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting followers count: " + error.getMessage());
                        errorMessageLiveData.setValue("Error getting followers count: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting following count: " + error.getMessage());
                errorMessageLiveData.setValue("Error getting following count: " + error.getMessage());
            }
        });
    }

    // Load followers and following counts for a user
    public void loadFollowCounts(String userId) {
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    followersCountLiveData.setValue(user.getFollowersCount());
                    followingCountLiveData.setValue(user.getFollowingCount());
                } else {
                    followersCountLiveData.setValue(0);
                    followingCountLiveData.setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading follow counts: " + error.getMessage());
                errorMessageLiveData.setValue("Error loading follow counts: " + error.getMessage());
            }
        });
    }
} 