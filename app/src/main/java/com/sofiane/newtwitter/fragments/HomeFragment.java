package com.sofiane.newtwitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

    // Variable pour stocker le post auquel on répond
    private Post replyToPost;

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
            
            // Initialize Toolbar
            setupToolbar();
            
            // Initialize RecyclerView
            setupRecyclerView();
            
            // Observe posts
            observePosts();
            
            // Observe errors
            observeErrors();
            
            // Setup SwipeRefreshLayout
            binding.swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
            
            // Setup FAB
            binding.createPostFab.setOnClickListener(v -> navigateToCreatePost());
            
            // Force refresh posts
            Log.d(TAG, "Forcing refresh of posts");
            binding.loadingProgressBar.setVisibility(View.VISIBLE);
            postViewModel.loadPosts();
            
            // Afficher un message pour indiquer que le chargement est en cours
            Toast.makeText(requireContext(), "Chargement des posts...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing home feed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupToolbar() {
        try {
            // Set profile image in header
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Si l'utilisateur a une photo de profil, on pourrait la charger ici
                // Pour l'instant, on utilise l'icône par défaut
            }
            
            // Configurer le logo
            binding.logoImageView.setOnClickListener(v -> {
                // Scroll to top when logo is clicked
                binding.postsRecyclerView.smoothScrollToPosition(0);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage(), e);
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
            Log.d(TAG, "Starting to observe posts from ViewModel");
            postViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
                Log.d(TAG, "Posts data changed: " + (posts != null ? posts.size() : 0) + " posts");
                
                // Toujours arrêter les indicateurs de chargement
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.loadingProgressBar.setVisibility(View.GONE);
                
                // Mettre à jour l'adaptateur même si la liste est vide
                postAdapter.setPosts(posts);
                
                // Show empty state if no posts
                if (posts == null || posts.isEmpty()) {
                    Log.d(TAG, "No posts available, showing empty state");
                    binding.emptyStateTextView.setVisibility(View.VISIBLE);
                    binding.postsRecyclerView.setVisibility(View.GONE);
                    
                    // Ajouter une animation pour attirer l'attention
                    binding.emptyStateTextView.setAlpha(0f);
                    binding.emptyStateTextView.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start();
                    
                    // Mettre en évidence le bouton d'ajout
                    binding.createPostFab.setScaleX(1.2f);
                    binding.createPostFab.setScaleY(1.2f);
                    binding.createPostFab.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(500)
                        .start();
                } else {
                    Log.d(TAG, "Posts available, showing RecyclerView");
                    binding.emptyStateTextView.setVisibility(View.GONE);
                    binding.postsRecyclerView.setVisibility(View.VISIBLE);
                    
                    // Log each post for debugging
                    for (Post post : posts) {
                        Log.d(TAG, "Post: " + post.getId() + ", User: " + post.getUsername() + ", Content: " + post.getContent());
                    }
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
            
            // Afficher un toast avec l'erreur
            Toast.makeText(requireContext(), "Error loading posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Log.d(TAG, "Refreshing posts");
            binding.swipeRefreshLayout.setRefreshing(true);
            binding.loadingProgressBar.setVisibility(View.VISIBLE);
            binding.emptyStateTextView.setVisibility(View.GONE);
            
            // Définir un timeout pour arrêter le chargement si ça prend trop de temps
            new Handler().postDelayed(() -> {
                if (binding != null && binding.swipeRefreshLayout.isRefreshing()) {
                    Log.w(TAG, "Loading posts timed out after 20 seconds");
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.loadingProgressBar.setVisibility(View.GONE);
                    
                    // Vérifier si des posts sont déjà affichés
                    if (postAdapter.getItemCount() == 0) {
                        binding.emptyStateTextView.setText(R.string.error_loading_posts);
                        binding.emptyStateTextView.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Loading timed out. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 20000); // 20 secondes de timeout
            
            postViewModel.loadPosts();
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing posts: " + e.getMessage(), e);
            binding.swipeRefreshLayout.setRefreshing(false);
            binding.loadingProgressBar.setVisibility(View.GONE);
            binding.emptyStateTextView.setText(R.string.error_loading_posts);
            binding.emptyStateTextView.setVisibility(View.VISIBLE);
            
            // Afficher un toast avec l'erreur
            Toast.makeText(requireContext(), "Error refreshing posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(requireContext(), "Post liked", Toast.LENGTH_SHORT).show();
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
    public void onPostRetweeted(Post post) {
        try {
            // Appeler la méthode du ViewModel pour retweeter le post
            postViewModel.retweetPost(post);
            Toast.makeText(requireContext(), "Post retweeté", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error retweeting post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error retweeting post", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onPostShared(Post post) {
        try {
            // Créer une intention de partage
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, post.getUsername() + ": " + post.getContent());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error sharing post", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostReplied(Post post) {
        try {
            // Stocker le post auquel on répond
            replyToPost = post;
            
            // Naviguer vers l'écran de création de post avec des informations supplémentaires
            Bundle args = new Bundle();
            args.putString("parent_post_id", post.getId());
            args.putString("parent_username", post.getUsername());
            Navigation.findNavController(requireView()).navigate(R.id.navigation_create_post, args);
        } catch (Exception e) {
            Log.e(TAG, "Error replying to post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error replying to post", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 