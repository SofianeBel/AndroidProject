package com.sofiane.newtwitter.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FollowManager {

    private final DatabaseReference mDatabase;
    private final FirebaseUser currentUser;

    public interface FollowListener {
        void onSuccess();
        void onError(String message);
    }

    public interface FollowStatusListener {
        void onStatus(boolean isFollowing);
        void onError(String message);
    }

    public interface CountListener {
        void onCount(int count);
        void onError(String message);
    }

    public FollowManager() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void followUser(String targetUserId, final FollowListener listener) {
        if (currentUser == null) {
            if (listener != null) {
                listener.onError("Utilisateur non connecté");
            }
            return;
        }

        // Ajouter l'utilisateur cible à la liste des utilisateurs suivis par l'utilisateur actuel
        mDatabase.child("follows").child(currentUser.getUid()).child("following").child(targetUserId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Ajouter l'utilisateur actuel à la liste des abonnés de l'utilisateur cible
                        mDatabase.child("follows").child(targetUserId).child("followers").child(currentUser.getUid()).setValue(true)
                                .addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful() && listener != null) {
                                        listener.onSuccess();
                                    } else if (listener != null) {
                                        listener.onError("Erreur lors de l'ajout aux abonnés");
                                    }
                                });
                    } else if (listener != null) {
                        listener.onError("Erreur lors de l'abonnement");
                    }
                });
    }

    public void unfollowUser(String targetUserId, final FollowListener listener) {
        if (currentUser == null) {
            if (listener != null) {
                listener.onError("Utilisateur non connecté");
            }
            return;
        }

        // Supprimer l'utilisateur cible de la liste des utilisateurs suivis par l'utilisateur actuel
        mDatabase.child("follows").child(currentUser.getUid()).child("following").child(targetUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Supprimer l'utilisateur actuel de la liste des abonnés de l'utilisateur cible
                        mDatabase.child("follows").child(targetUserId).child("followers").child(currentUser.getUid()).removeValue()
                                .addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful() && listener != null) {
                                        listener.onSuccess();
                                    } else if (listener != null) {
                                        listener.onError("Erreur lors de la suppression des abonnés");
                                    }
                                });
                    } else if (listener != null) {
                        listener.onError("Erreur lors du désabonnement");
                    }
                });
    }

    public void checkFollowStatus(String targetUserId, final FollowStatusListener listener) {
        if (currentUser == null) {
            if (listener != null) {
                listener.onError("Utilisateur non connecté");
            }
            return;
        }

        mDatabase.child("follows").child(currentUser.getUid()).child("following").child(targetUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (listener != null) {
                            listener.onStatus(dataSnapshot.exists());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (listener != null) {
                            listener.onError(databaseError.getMessage());
                        }
                    }
                });
    }

    public void getFollowersCount(String userId, final CountListener listener) {
        mDatabase.child("follows").child(userId).child("followers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (listener != null) {
                            listener.onCount((int) dataSnapshot.getChildrenCount());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (listener != null) {
                            listener.onError(databaseError.getMessage());
                        }
                    }
                });
    }

    public void getFollowingCount(String userId, final CountListener listener) {
        mDatabase.child("follows").child(userId).child("following")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (listener != null) {
                            listener.onCount((int) dataSnapshot.getChildrenCount());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (listener != null) {
                            listener.onError(databaseError.getMessage());
                        }
                    }
                });
    }
} 