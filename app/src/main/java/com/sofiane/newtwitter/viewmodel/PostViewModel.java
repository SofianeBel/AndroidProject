package com.sofiane.newtwitter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.repository.PostRepository;
import java.util.List;

public class PostViewModel extends ViewModel {
    private final PostRepository postRepository;
    private final MediatorLiveData<List<Post>> posts = new MediatorLiveData<>();
    private final MediatorLiveData<String> errorMessage = new MediatorLiveData<>();

    public PostViewModel() {
        postRepository = PostRepository.getInstance();
        
        // Observe posts from repository
        posts.addSource(postRepository.getAllPostsLiveData(), posts::setValue);
        
        // Observe error messages from repository
        errorMessage.addSource(postRepository.getErrorMessageLiveData(), errorMessage::setValue);
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadPosts() {
        postRepository.loadAllPosts();
    }

    public void createPost(String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Post content cannot be empty");
            return;
        }

        postRepository.createPost(content);
    }
    
    // For testing or when user info is known
    public void createPost(String userId, String username, String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Post content cannot be empty");
            return;
        }

        postRepository.createPost(userId, username, content);
    }

    public void likePost(String postId) {
        postRepository.likePost(postId);
    }

    public void deletePost(String postId) {
        postRepository.deletePost(postId);
    }
} 