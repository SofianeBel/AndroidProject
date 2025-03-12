package com.sofiane.newtwitter.repository;

import android.util.Log;
import com.sofiane.newtwitter.model.Post;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostRepository {
    private static final String TAG = "PostRepository";
    private static PostRepository instance;
    private Map<String, Post> posts;
    private int nextId = 1;

    private PostRepository() {
        posts = new HashMap<>();
        try {
            // Add some mock data
            addMockPosts();
            Log.d(TAG, "Mock posts added successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error adding mock posts: " + e.getMessage());
        }
    }

    public static PostRepository getInstance() {
        if (instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    private void addMockPosts() {
        try {
            createPost("1", "user1", "Hello MicroVoice! This is my first post!");
            createPost("2", "user2", "Just discovered this amazing app!");
            createPost("1", "user1", "The weather is beautiful today!");
            Log.d(TAG, "Added 3 mock posts");
        } catch (Exception e) {
            Log.e(TAG, "Error in addMockPosts: " + e.getMessage());
        }
    }

    public Post createPost(String userId, String username, String content) {
        try {
            String postId = String.valueOf(nextId++);
            // Create a new Post with all required parameters
            Post post = new Post(
                postId,
                userId,
                username,
                content,
                null, // No image URL for mock posts
                new Date(), // Current date
                0 // Initial like count is 0
            );
            posts.put(postId, post);
            Log.d(TAG, "Created post with ID: " + postId);
            return post;
        } catch (Exception e) {
            Log.e(TAG, "Error creating post: " + e.getMessage());
            return null;
        }
    }

    // Overload for backward compatibility
    public Post createPost(String userId, String content) {
        try {
            // Default username to "User" + userId if not provided
            return createPost(userId, "User" + userId, content);
        } catch (Exception e) {
            Log.e(TAG, "Error in createPost overload: " + e.getMessage());
            return null;
        }
    }

    public List<Post> getAllPosts() {
        try {
            List<Post> allPosts = new ArrayList<>(posts.values());
            // Sort by creation date (newest first)
            allPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
            Log.d(TAG, "Retrieved " + allPosts.size() + " posts");
            return allPosts;
        } catch (Exception e) {
            Log.e(TAG, "Error getting all posts: " + e.getMessage());
            return new ArrayList<>(); // Return empty list instead of null
        }
    }

    public List<Post> getPostsByUser(String userId) {
        try {
            List<Post> userPosts = new ArrayList<>();
            for (Post post : posts.values()) {
                if (post.getUserId().equals(userId)) {
                    userPosts.add(post);
                }
            }
            Log.d(TAG, "Retrieved " + userPosts.size() + " posts for user " + userId);
            return userPosts;
        } catch (Exception e) {
            Log.e(TAG, "Error getting posts for user " + userId + ": " + e.getMessage());
            return new ArrayList<>(); // Return empty list instead of null
        }
    }

    public void likePost(String postId) {
        try {
            Post post = posts.get(postId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + 1);
                Log.d(TAG, "Liked post " + postId + ", new count: " + post.getLikeCount());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error liking post " + postId + ": " + e.getMessage());
        }
    }

    public void deletePost(String postId) {
        try {
            posts.remove(postId);
            Log.d(TAG, "Deleted post " + postId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting post " + postId + ": " + e.getMessage());
        }
    }
} 