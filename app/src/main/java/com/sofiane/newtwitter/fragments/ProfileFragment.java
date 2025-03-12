package com.sofiane.newtwitter.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.adapter.PostAdapter;
import com.sofiane.newtwitter.databinding.FragmentProfileBinding;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.model.User;
import com.sofiane.newtwitter.utils.ProfileIconHelper;
import com.sofiane.newtwitter.viewmodel.FollowViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements PostAdapter.OnPostInteractionListener {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private DatabaseReference postsRef;
    private PostAdapter postAdapter;
    private FollowViewModel followViewModel;
    private List<Post> userPosts = new ArrayList<>();
    private String userId;
    private boolean isCurrentUserProfile = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");
        postsRef = database.getReference("posts");
        
        // Initialize ViewModel
        followViewModel = new ViewModelProvider(requireActivity()).get(FollowViewModel.class);

        // Check if we're viewing another user's profile
        if (getArguments() != null && getArguments().containsKey("userId")) {
            userId = getArguments().getString("userId");
            isCurrentUserProfile = currentUser != null && userId.equals(currentUser.getUid());
        } else if (currentUser != null) {
            userId = currentUser.getUid();
            isCurrentUserProfile = true;
        } else {
            Toast.makeText(requireContext(), "Vous devez être connecté pour voir votre profil", Toast.LENGTH_SHORT).show();
            // Navigate to login screen or handle appropriately
            return;
        }

        // Set up RecyclerView for posts
        setupRecyclerView();
        
        // Set up follow button
        setupFollowButton();
        
        // Observe follow status changes
        observeFollowStatus();

        // Set up edit profile button
        binding.editProfileButton.setOnClickListener(v -> {
            if (isCurrentUserProfile) {
                // Navigate to edit profile screen
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
            }
        });

        // Show/hide edit button based on whose profile we're viewing
        binding.editProfileButton.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.GONE);
        binding.followButton.setVisibility(isCurrentUserProfile ? View.GONE : View.VISIBLE);
        
        // Set up followers and following click listeners
        binding.followersCount.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putInt("listType", FollowListFragment.TYPE_FOLLOWERS);
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_followListFragment, args);
        });
        
        binding.followingCount.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putInt("listType", FollowListFragment.TYPE_FOLLOWING);
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_followListFragment, args);
        });

        // Load user profile and posts
        loadUserProfile();
        loadUserPosts();
        
        // Load follow counts
        followViewModel.loadFollowCounts(userId);
    }
    
    private void setupFollowButton() {
        if (!isCurrentUserProfile && currentUser != null) {
            binding.followButton.setOnClickListener(v -> {
                if (binding.followButton.getText().toString().equals(getString(R.string.follow))) {
                    followViewModel.followUser(userId);
                } else {
                    followViewModel.unfollowUser(userId);
                }
            });
            
            // Check if current user is following this profile
            followViewModel.checkFollowStatus(userId);
        }
    }
    
    private void observeFollowStatus() {
        // Observe follow status
        followViewModel.getFollowStatus().observe(getViewLifecycleOwner(), isFollowing -> {
            if (isFollowing) {
                binding.followButton.setText(R.string.unfollow);
                binding.followButton.setBackgroundResource(R.drawable.button_outline_background);
            } else {
                binding.followButton.setText(R.string.follow);
                binding.followButton.setBackgroundResource(R.drawable.button_primary_background);
            }
        });
        
        // Observe followers count
        followViewModel.getFollowersCount().observe(getViewLifecycleOwner(), count -> {
            binding.followersCount.setText(count + " " + getString(R.string.followers));
        });
        
        // Observe following count
        followViewModel.getFollowingCount().observe(getViewLifecycleOwner(), count -> {
            binding.followingCount.setText(count + " " + getString(R.string.following));
        });
        
        // Observe error messages
        followViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        binding.userPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.userPostsRecyclerView.setAdapter(postAdapter);
    }

    private void loadUserProfile() {
        // Réinitialiser les champs de texte pour éviter d'afficher des données obsolètes
        if (binding != null) {
            binding.nameText.setText("");
            binding.usernameText.setText("");
            binding.bioText.setText("");
        } else {
            Log.e(TAG, "loadUserProfile: binding is null");
            return;
        }

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Vérifier si le binding est toujours valide
                if (binding == null) {
                    Log.e(TAG, "onDataChange: binding is null, fragment may have been destroyed");
                    return;
                }
                
                // Vérifier si les données existent
                if (snapshot.exists()) {
                    try {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Log.d(TAG, "Profil utilisateur chargé avec succès: " + user.getUsername());
                            Log.d(TAG, "Index chargés: iconIndex=" + user.getProfileIconIndex() + ", colorIndex=" + user.getProfileColorIndex());
                            updateUI(user);
                            return; // Sortir de la méthode car tout est OK
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la conversion des données utilisateur: " + e.getMessage(), e);
                        // Continuer pour créer un nouveau profil
                    }
                }
                
                // Si on arrive ici, c'est que les données n'existent pas ou sont invalides
                Log.w(TAG, "Aucun profil utilisateur trouvé ou données invalides, création d'un nouveau profil");
                
                // Créer un profil utilisateur temporaire pour l'affichage
                String displayName = "Utilisateur";
                if (currentUser != null && userId.equals(currentUser.getUid())) {
                    // Si c'est le profil de l'utilisateur actuel, on peut récupérer son nom depuis Firebase Auth
                    if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                        displayName = currentUser.getDisplayName();
                    }
                    
                    // Créer un nouveau profil utilisateur avec les valeurs par défaut
                    User newUser = new User(
                        userId,
                        displayName,
                        currentUser.getEmail()
                    );
                    
                    // Sauvegarder ce nouveau profil dans la base de données
                    usersRef.child(userId).setValue(newUser)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Nouveau profil utilisateur créé et sauvegardé");
                            // Le listener ValueEventListener sera déclenché à nouveau après la sauvegarde
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erreur lors de la sauvegarde du nouveau profil: " + e.getMessage(), e);
                            Toast.makeText(requireContext(), "Erreur lors de la création du profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    // Si c'est le profil d'un autre utilisateur, afficher un message d'erreur
                    Toast.makeText(requireContext(), "Profil utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user profile: " + error.getMessage());
                Toast.makeText(requireContext(), "Erreur lors du chargement du profil: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(User user) {
        if (binding == null) {
            Log.e(TAG, "updateUI: binding is null, fragment may have been destroyed");
            return;
        }
        
        Log.d(TAG, "Updating UI with user data: " + user.getUserId());
        
        // Set user info
        binding.nameText.setText(user.getUsername());
        binding.usernameText.setText("@" + user.getUsername().toLowerCase().replace(" ", ""));
        
        // Set bio if available
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            binding.bioText.setText(user.getBio());
            binding.bioText.setVisibility(View.VISIBLE);
        } else {
            binding.bioText.setVisibility(View.GONE);
        }

        // Afficher l'icône de profil colorée
        try {
            Drawable coloredIcon = ProfileIconHelper.getColoredProfileIcon(
                    requireContext(),
                    user.getProfileIconIndex(),
                    user.getProfileColorIndex()
            );
            binding.profileImage.setImageDrawable(coloredIcon);
            Log.d(TAG, "Profile icon set with icon index: " + user.getProfileIconIndex() + 
                    ", color index: " + user.getProfileColorIndex());
        } catch (Exception e) {
            Log.e(TAG, "Error setting profile icon: " + e.getMessage(), e);
            // Fallback to default icon
            binding.profileImage.setImageResource(R.drawable.ic_profile_person);
        }

        // Définir une couleur de bannière basée sur la couleur de profil
        try {
            int bannerColor = ProfileIconHelper.getProfileColor(requireContext(), user.getProfileColorIndex());
            // Utiliser une version plus claire de la couleur pour la bannière
            int lighterColor = lightenColor(bannerColor, 0.3f);
            binding.coverImage.setBackgroundColor(lighterColor);
            Log.d(TAG, "Banner color set based on profile color index: " + user.getProfileColorIndex());
        } catch (Exception e) {
            Log.e(TAG, "Error setting banner color: " + e.getMessage(), e);
            // Fallback to default color
            binding.coverImage.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light));
        }
        
        // Update follow counts
        binding.followersCount.setText(user.getFollowersCount() + " " + getString(R.string.followers));
        binding.followingCount.setText(user.getFollowingCount() + " " + getString(R.string.following));
    }

    /**
     * Éclaircit une couleur en ajoutant du blanc
     * @param color La couleur à éclaircir
     * @param factor Le facteur d'éclaircissement (0.0 à 1.0)
     * @return La couleur éclaircie
     */
    private int lightenColor(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) + 255 * factor));
        int green = (int) ((Color.green(color) * (1 - factor) + 255 * factor));
        int blue = (int) ((Color.blue(color) * (1 - factor) + 255 * factor));
        return Color.rgb(red, green, blue);
    }

    private void loadUserPosts() {
        // Query posts by this user
        Query query = postsRef.orderByChild("userId").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Vérifier si le binding est toujours valide
                if (binding == null) {
                    Log.e(TAG, "Binding is null in loadUserPosts.onDataChange");
                    return;
                }
                
                userPosts.clear();
                int postCount = 0;
                
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Only include original posts and retweets, not replies
                        if (!post.isReply()) {
                            userPosts.add(post);
                        }
                        postCount++;
                    }
                }
                
                // Sort by creation date (newest first)
                userPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                
                // Update the adapter
                postAdapter.setPosts(userPosts);
                
                // Update post count
                binding.postsCount.setText(postCount + " " + getString(R.string.posts));
                
                // Show/hide empty state
                if (userPosts.isEmpty()) {
                    binding.userPostsRecyclerView.setVisibility(View.GONE);
                    // You might want to add an empty state view here
                } else {
                    binding.userPostsRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (binding == null) {
                    Log.e(TAG, "Binding is null in loadUserPosts.onCancelled");
                    return;
                }
                
                Log.e(TAG, "Error loading user posts: " + databaseError.getMessage());
                Toast.makeText(requireContext(), "Erreur lors du chargement des posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Implémentation des méthodes de l'interface OnPostInteractionListener
    @Override
    public void onPostLiked(Post post) {
        // Gérer le like d'un post
        Toast.makeText(requireContext(), "Like non implémenté dans le profil", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostClicked(Post post) {
        // Naviguer vers le détail du post
        Bundle args = new Bundle();
        args.putString("post_id", post.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_postDetailFragment, args);
    }

    @Override
    public void onPostRetweeted(Post post) {
        // Gérer le retweet d'un post
        Toast.makeText(requireContext(), "Retweet non implémenté dans le profil", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostShared(Post post) {
        // Gérer le partage d'un post
        Toast.makeText(requireContext(), "Partage non implémenté dans le profil", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostReplied(Post post) {
        // Gérer la réponse à un post
        Toast.makeText(requireContext(), "Réponse non implémentée dans le profil", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserProfileClicked(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(requireContext(), "ID utilisateur invalide", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Si c'est le même profil que celui actuellement affiché, ne rien faire
            if (userId.equals(this.userId)) {
                return;
            }
            
            Log.d(TAG, "Navigation vers le profil utilisateur: " + userId);
            Bundle args = new Bundle();
            args.putString("userId", userId);
            Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_self, args);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la navigation vers le profil: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur lors de la navigation vers le profil", Toast.LENGTH_SHORT).show();
        }
    }
} 