package com.sofiane.newtwitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.adapter.PostAdapter;
import com.sofiane.newtwitter.databinding.FragmentHomeBinding;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.viewmodel.PostViewModel;

public class HomeFragment extends Fragment implements PostAdapter.OnPostInteractionListener {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private PostViewModel postViewModel;
    private PostAdapter postAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Log.d(TAG, "Creating HomeFragment view");
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error creating HomeFragment view: " + e.getMessage(), e);
            // Créer une vue simple en cas d'erreur
            TextView errorView = new TextView(getContext());
            errorView.setText("Error loading home feed. Please try again later.");
            errorView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            errorView.setGravity(Gravity.CENTER);
            errorView.setPadding(50, 50, 50, 50);
            return errorView;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Initialize ViewModel
            postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
            
            // Initialize RecyclerView and adapter
            setupRecyclerView();
            
            // Set up SwipeRefreshLayout
            binding.swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
            
            // Set up FAB for creating new posts
            binding.createPostFab.setOnClickListener(v -> navigateToCreatePost());
            
            // Observe posts from ViewModel
            observePosts();
            
            // Observe error messages
            observeErrors();
            
            // Load posts
            refreshPosts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing home feed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupRecyclerView() {
        try {
            postAdapter = new PostAdapter(this);
            binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.postsRecyclerView.setAdapter(postAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage(), e);
        }
    }
    
    private void observePosts() {
        try {
            postViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
                postAdapter.setPosts(posts);
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.loadingProgressBar.setVisibility(View.GONE);
                
                // Show empty state if no posts
                if (posts == null || posts.isEmpty()) {
                    binding.emptyStateTextView.setVisibility(View.VISIBLE);
                    binding.postsRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyStateTextView.setVisibility(View.GONE);
                    binding.postsRecyclerView.setVisibility(View.VISIBLE);
                }
                
                Log.d(TAG, "Posts updated: " + (posts != null ? posts.size() : 0) + " posts");
            });
        } catch (Exception e) {
            Log.e(TAG, "Error observing posts: " + e.getMessage(), e);
            binding.swipeRefreshLayout.setRefreshing(false);
            binding.loadingProgressBar.setVisibility(View.GONE);
            binding.emptyStateTextView.setText(R.string.error_loading_posts);
            binding.emptyStateTextView.setVisibility(View.VISIBLE);
            binding.postsRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void observeErrors() {
        try {
            postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error observing error messages: " + e.getMessage(), e);
        }
    }
    
    private void refreshPosts() {
        try {
            binding.swipeRefreshLayout.setRefreshing(true);
            binding.loadingProgressBar.setVisibility(View.VISIBLE);
            binding.emptyStateTextView.setVisibility(View.GONE);
            postViewModel.loadPosts();
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing posts: " + e.getMessage(), e);
            binding.swipeRefreshLayout.setRefreshing(false);
            binding.loadingProgressBar.setVisibility(View.GONE);
            binding.emptyStateTextView.setText(R.string.error_loading_posts);
            binding.emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
    
    private void navigateToCreatePost() {
        try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_create_post);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to create post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error navigating to create post", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostLiked(Post post) {
        try {
            postViewModel.likePost(post.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error liking post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error liking post", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostClicked(Post post) {
        try {
            // Pour l'instant, nous affichons simplement un toast
            Toast.makeText(requireContext(), "Post clicked: " + post.getContent(), Toast.LENGTH_SHORT).show();
            // Dans une future version, nous pourrions naviguer vers un écran de détail du post
        } catch (Exception e) {
            Log.e(TAG, "Error handling post click: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 