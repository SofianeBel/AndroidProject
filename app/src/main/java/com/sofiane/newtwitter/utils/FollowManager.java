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

/**
 * Gestionnaire des relations de suivi entre utilisateurs.
 * Cette classe fournit des méthodes pour suivre/ne plus suivre des utilisateurs
 * et pour obtenir des informations sur les relations de suivi.
 * Elle interagit directement avec Firebase Realtime Database.
 */
public class FollowManager {

    private final DatabaseReference mDatabase;
    private final FirebaseUser currentUser;

    /**
     * Interface de callback pour les opérations de suivi/désabonnement.
     */
    public interface FollowListener {
        /**
         * Appelé lorsque l'opération est réussie.
         */
        void onSuccess();
        
        /**
         * Appelé lorsqu'une erreur se produit.
         * 
         * @param message Message d'erreur
         */
        void onError(String message);
    }

    /**
     * Interface de callback pour vérifier le statut de suivi.
     */
    public interface FollowStatusListener {
        /**
         * Appelé avec le statut de suivi.
         * 
         * @param isFollowing true si l'utilisateur courant suit l'utilisateur cible, false sinon
         */
        void onStatus(boolean isFollowing);
        
        /**
         * Appelé lorsqu'une erreur se produit.
         * 
         * @param message Message d'erreur
         */
        void onError(String message);
    }

    /**
     * Interface de callback pour obtenir des compteurs (followers/following).
     */
    public interface CountListener {
        /**
         * Appelé avec le nombre d'utilisateurs.
         * 
         * @param count Nombre d'utilisateurs (followers ou following)
         */
        void onCount(int count);
        
        /**
         * Appelé lorsqu'une erreur se produit.
         * 
         * @param message Message d'erreur
         */
        void onError(String message);
    }

    /**
     * Constructeur qui initialise les références Firebase.
     */
    public FollowManager() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Permet à l'utilisateur courant de suivre un utilisateur cible.
     * Met à jour les listes "following" et "followers" dans Firebase.
     * 
     * @param targetUserId ID de l'utilisateur à suivre
     * @param listener Callback pour notifier du résultat de l'opération
     */
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

    /**
     * Permet à l'utilisateur courant de ne plus suivre un utilisateur cible.
     * Met à jour les listes "following" et "followers" dans Firebase.
     * 
     * @param targetUserId ID de l'utilisateur à ne plus suivre
     * @param listener Callback pour notifier du résultat de l'opération
     */
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

    /**
     * Vérifie si l'utilisateur courant suit un utilisateur cible.
     * 
     * @param targetUserId ID de l'utilisateur cible
     * @param listener Callback pour notifier du résultat de la vérification
     */
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

    /**
     * Obtient le nombre d'abonnés (followers) d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param listener Callback pour notifier du résultat
     */
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

    /**
     * Obtient le nombre d'utilisateurs suivis (following) par un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param listener Callback pour notifier du résultat
     */
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