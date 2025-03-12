package com.sofiane.newtwitter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.repository.PostRepository;
import java.util.List;

public class PostViewModel extends ViewModel {
    private final PostRepository postRepository;
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PostViewModel() {
        postRepository = PostRepository.getInstance();
        loadPosts();
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadPosts() {
        posts.setValue(postRepository.getAllPosts());
    }

    public void createPost(String userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Post content cannot be empty");
            return;
        }

        postRepository.createPost(userId, content);
        loadPosts(); // Reload posts after creating a new one
    }

    public void likePost(String postId) {
        postRepository.likePost(postId);
        loadPosts(); // Reload to update like count
    }

    public void deletePost(String postId) {
        postRepository.deletePost(postId);
        loadPosts(); // Reload after deletion
    }
} 