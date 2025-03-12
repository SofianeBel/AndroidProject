package com.sofiane.newtwitter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sofiane.newtwitter.utils.FollowManager;

public class FollowViewModel extends ViewModel {
    private final FollowManager followManager;
    private final MutableLiveData<Boolean> followStatus = new MutableLiveData<>();
    private final MutableLiveData<Integer> followersCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> followingCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public FollowViewModel() {
        followManager = new FollowManager();
    }

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

    public LiveData<Boolean> getFollowStatus() {
        return followStatus;
    }

    public LiveData<Integer> getFollowersCount() {
        return followersCount;
    }

    public LiveData<Integer> getFollowingCount() {
        return followingCount;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
} 