package com.sofiane.newtwitter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sofiane.newtwitter.utils.FollowManager;

/**
 * ViewModel responsable de la gestion des relations de suivi entre utilisateurs.
 * Cette classe utilise FollowManager pour interagir avec Firebase et fournit des données
 * observables sur l'état des relations de suivi, les compteurs et les messages.
 */
public class FollowViewModel extends ViewModel {
    private final FollowManager followManager;
    private final MutableLiveData<Boolean> followStatus = new MutableLiveData<>();
    private final MutableLiveData<Integer> followersCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    /**
     * Constructeur qui initialise le FollowManager.
     */
    public FollowViewModel() {
        followManager = new FollowManager();
    }

    /**
     * Permet à l'utilisateur courant de suivre un utilisateur cible.
     * Met à jour les LiveData en fonction du résultat.
     *
     * @param targetUserId ID de l'utilisateur à suivre
     */
    public void followUser(String targetUserId) {
        followManager.followUser(targetUserId, new FollowManager.FollowListener() {
            @Override
            public void onSuccess() {
                followStatus.postValue(true);
                successMessage.postValue("Vous suivez maintenant cet utilisateur");
                // Refresh counts
                loadFollowCounts(targetUserId);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Permet à l'utilisateur courant de ne plus suivre un utilisateur cible.
     * Met à jour les LiveData en fonction du résultat.
     *
     * @param targetUserId ID de l'utilisateur à ne plus suivre
     */
    public void unfollowUser(String targetUserId) {
        followManager.unfollowUser(targetUserId, new FollowManager.FollowListener() {
            @Override
            public void onSuccess() {
                followStatus.postValue(false);
                successMessage.postValue("Vous ne suivez plus cet utilisateur");
                // Refresh counts
                loadFollowCounts(targetUserId);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Vérifie si l'utilisateur courant suit un utilisateur cible.
     * Met à jour le LiveData followStatus avec le résultat.
     *
     * @param targetUserId ID de l'utilisateur cible
     */
    public void checkFollowStatus(String targetUserId) {
        followManager.checkFollowStatus(targetUserId, new FollowManager.FollowStatusListener() {
            @Override
            public void onStatus(boolean isFollowing) {
                followStatus.postValue(isFollowing);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Charge les compteurs de followers et following pour un utilisateur.
     * Met à jour les LiveData followersCount et followingCount avec les résultats.
     *
     * @param userId ID de l'utilisateur
     */
    public void loadFollowCounts(String userId) {
        // Load followers count
        followManager.getFollowersCount(userId, new FollowManager.CountListener() {
            @Override
            public void onCount(int count) {
                followersCount.postValue(count);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });

        // Load following count
        followManager.getFollowingCount(userId, new FollowManager.CountListener() {
            @Override
            public void onCount(int count) {
                followingCount.postValue(count);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Obtient le LiveData contenant l'état de suivi.
     *
     * @return LiveData contenant true si l'utilisateur courant suit l'utilisateur cible, false sinon
     */
    public LiveData<Boolean> getFollowStatus() {
        return followStatus;
    }

    /**
     * Obtient le LiveData contenant le nombre de followers.
     *
     * @return LiveData contenant le nombre de followers
     */
    public LiveData<Integer> getFollowersCount() {
        return followersCount;
    }

    /**
     * Obtient le LiveData contenant le nombre d'utilisateurs suivis.
     *
     * @return LiveData contenant le nombre d'utilisateurs suivis
     */
    public LiveData<Integer> getFollowingCount() {
        return followingCount;
    }

    /**
     * Obtient le LiveData contenant les messages d'erreur.
     *
     * @return LiveData contenant les messages d'erreur
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Obtient le LiveData contenant les messages de succès.
     *
     * @return LiveData contenant les messages de succès
     */
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
} 