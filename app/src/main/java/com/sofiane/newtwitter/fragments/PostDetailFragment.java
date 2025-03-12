package com.sofiane.newtwitter.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.adapter.PostAdapter;
import com.sofiane.newtwitter.databinding.FragmentPostDetailBinding;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.utils.ProfileIconHelper;
import com.sofiane.newtwitter.viewmodel.PostViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostDetailFragment extends Fragment implements PostAdapter.OnPostInteractionListener {
    private static final String TAG = "PostDetailFragment";
    private FragmentPostDetailBinding binding;
    private PostViewModel postViewModel;
    private PostAdapter repliesAdapter;
    private Post currentPost;
    private String postId;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");

        // Récupérer l'ID du post depuis les arguments
        if (getArguments() != null) {
            postId = getArguments().getString("post_id");
            if (postId == null) {
                Toast.makeText(requireContext(), "Erreur: ID du post manquant", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
                return;
            }
        } else {
            Toast.makeText(requireContext(), "Erreur: arguments manquants", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Initialiser le ViewModel
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        // Configurer la toolbar
        setupToolbar();

        // Configurer le RecyclerView pour les réponses
        setupRepliesRecyclerView();

        // Observer les posts pour trouver le post actuel et ses réponses
        observePosts();

        // Observer les messages d'erreur
        observeErrors();

        // Configurer les boutons d'interaction
        setupInteractionButtons();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
        binding.toolbar.setTitle("Post");
    }

    private void setupRepliesRecyclerView() {
        repliesAdapter = new PostAdapter(this);
        binding.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.repliesRecyclerView.setAdapter(repliesAdapter);
    }

    private void observePosts() {
        postViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null && !posts.isEmpty()) {
                // Trouver le post actuel
                for (Post post : posts) {
                    if (post.getId().equals(postId)) {
                        currentPost = post;
                        displayPostDetails(post);
                        break;
                    }
                }

                // Filtrer les réponses à ce post
                if (currentPost != null) {
                    List<Post> replies = posts.stream()
                            .filter(post -> post.isReply() && postId.equals(post.getParentId()))
                            .collect(Collectors.toList());
                    
                    updateRepliesUI(replies);
                }
            }
        });
    }

    private void displayPostDetails(Post post) {
        // Afficher les détails du post
        String userIdToLoad = post.isRetweet() ? post.getOriginalUserId() : post.getUserId();
        loadUserProfileIcon(userIdToLoad, binding.profileImageView);
        
        binding.usernameTextView.setText(post.getUsername());
        binding.handleTextView.setText("@" + post.getUsername().toLowerCase().replace(" ", ""));
        binding.contentTextView.setText(post.getContent());
        binding.timeTextView.setText(post.getRelativeTime());
        
        // Configurer les clics sur le nom d'utilisateur et la photo de profil
        View.OnClickListener profileClickListener = v -> navigateToUserProfile(userIdToLoad);
        binding.profileImageView.setOnClickListener(profileClickListener);
        binding.usernameTextView.setOnClickListener(profileClickListener);
        binding.handleTextView.setOnClickListener(profileClickListener);
        
        // Afficher les compteurs
        binding.likeCountTextView.setText(String.valueOf(post.getLikeCount()));
        binding.retweetCountTextView.setText(String.valueOf(post.getRetweetCount()));
        binding.commentCountTextView.setText(String.valueOf(post.getCommentCount()));
        
        // Afficher l'image si disponible
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            binding.postImageView.setVisibility(View.VISIBLE);
            // Ici, vous devriez utiliser une bibliothèque comme Glide ou Picasso pour charger l'image
            // Glide.with(this).load(post.getImageUrl()).into(binding.postImageView);
        } else {
            binding.postImageView.setVisibility(View.GONE);
        }
        
        // Afficher les informations de retweet si c'est un retweet
        if (post.isRetweet()) {
            binding.retweetedByLayout.setVisibility(View.VISIBLE);
            binding.retweetedByTextView.setText(getString(R.string.retweeted_by, post.getUsername()));
            binding.usernameTextView.setText(post.getOriginalUsername());
        } else {
            binding.retweetedByLayout.setVisibility(View.GONE);
        }
        
        // Mettre à jour l'état des boutons d'interaction
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Ici, vous pourriez vérifier si l'utilisateur a déjà liké ou retweeté ce post
            // et mettre à jour l'apparence des boutons en conséquence
        }
    }

    private void updateRepliesUI(List<Post> replies) {
        if (replies.isEmpty()) {
            binding.noRepliesTextView.setVisibility(View.VISIBLE);
            binding.repliesRecyclerView.setVisibility(View.GONE);
        } else {
            binding.noRepliesTextView.setVisibility(View.GONE);
            binding.repliesRecyclerView.setVisibility(View.VISIBLE);
            repliesAdapter.setPosts(replies);
        }
    }

    private void observeErrors() {
        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInteractionButtons() {
        // Bouton Like
        binding.likeButton.setOnClickListener(v -> {
            if (currentPost != null) {
                onPostLiked(currentPost);
            }
        });

        // Bouton Retweet
        binding.retweetButton.setOnClickListener(v -> {
            if (currentPost != null) {
                onPostRetweeted(currentPost);
            }
        });

        // Bouton Répondre
        binding.replyButton.setOnClickListener(v -> {
            if (currentPost != null) {
                onPostReplied(currentPost);
            }
        });

        // Bouton Partager
        binding.shareButton.setOnClickListener(v -> {
            if (currentPost != null) {
                onPostShared(currentPost);
            }
        });
    }

    @Override
    public void onPostLiked(Post post) {
        try {
            if (post.getId().equals(postId)) {
                // C'est le post principal
                postViewModel.likePost(post.getId());
                Toast.makeText(requireContext(), "Post liké", Toast.LENGTH_SHORT).show();
            } else {
                // C'est une réponse
                postViewModel.likePost(post.getId());
                Toast.makeText(requireContext(), "Réponse likée", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error liking post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur lors du like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostClicked(Post post) {
        // Si on clique sur une réponse, on pourrait ouvrir un nouveau détail pour cette réponse
        if (!post.getId().equals(postId)) {
            Bundle args = new Bundle();
            args.putString("post_id", post.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_postDetailFragment_self, args);
        }
    }

    @Override
    public void onPostRetweeted(Post post) {
        try {
            postViewModel.retweetPost(post);
            Toast.makeText(requireContext(), "Post retweeté", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error retweeting post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur lors du retweet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostShared(Post post) {
        try {
            // Implémenter le partage
            Toast.makeText(requireContext(), "Partage du post", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sharing post: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur lors du partage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostReplied(Post post) {
        try {
            // Naviguer vers le fragment de création de post avec les informations du post parent
            Bundle args = new Bundle();
            args.putString("parent_post_id", post.getId());
            args.putString("parent_username", post.getUsername());
            Navigation.findNavController(requireView()).navigate(R.id.action_postDetailFragment_to_createPostFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to reply: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur lors de la navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserProfileClicked(String userId) {
        navigateToUserProfile(userId);
    }

    /**
     * Charge l'icône de profil d'un utilisateur à partir de Firebase
     * @param userId L'ID de l'utilisateur
     * @param imageView La vue d'image à mettre à jour
     */
    private void loadUserProfileIcon(String userId, CircleImageView imageView) {
        if (userId == null || userId.isEmpty()) {
            // Si l'ID utilisateur est invalide, utiliser l'icône par défaut
            imageView.setImageResource(R.drawable.ic_profile_person);
            return;
        }
        
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Récupérer les index d'icône et de couleur
                        Integer iconIndex = snapshot.child("profileIconIndex").getValue(Integer.class);
                        Integer colorIndex = snapshot.child("profileColorIndex").getValue(Integer.class);
                        
                        if (iconIndex != null && colorIndex != null) {
                            Context context = imageView.getContext();
                            // Obtenir l'icône colorée
                            Drawable coloredIcon = ProfileIconHelper.getColoredProfileIcon(
                                    context, iconIndex, colorIndex);
                            
                            // Mettre à jour l'image de profil
                            imageView.setImageDrawable(coloredIcon);
                        } else {
                            // Utiliser l'icône par défaut si les index sont null
                            imageView.setImageResource(R.drawable.ic_profile_person);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du chargement de l'icône de profil: " + e.getMessage(), e);
                        // En cas d'erreur, utiliser l'icône par défaut
                        imageView.setImageResource(R.drawable.ic_profile_person);
                    }
                } else {
                    // Si l'utilisateur n'existe pas, utiliser l'icône par défaut
                    imageView.setImageResource(R.drawable.ic_profile_person);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erreur lors de la récupération des données utilisateur: " + error.getMessage());
                // En cas d'erreur, utiliser l'icône par défaut
                imageView.setImageResource(R.drawable.ic_profile_person);
            }
        });
    }
    
    /**
     * Navigue vers le profil de l'utilisateur
     * @param userId L'ID de l'utilisateur
     */
    private void navigateToUserProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "ID utilisateur invalide", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Bundle args = new Bundle();
            args.putString("userId", userId);
            Navigation.findNavController(requireView()).navigate(R.id.action_postDetailFragment_to_profileFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la navigation vers le profil: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur lors de la navigation vers le profil", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 