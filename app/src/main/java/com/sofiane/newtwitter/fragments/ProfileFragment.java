package com.sofiane.newtwitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

        // Set up edit profile button
        binding.editProfileButton.setOnClickListener(v -> {
            if (isCurrentUserProfile) {
                // Navigate to edit profile screen
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment);
            }
        });

        // Show/hide edit button based on whose profile we're viewing
        binding.editProfileButton.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.GONE);

        // Load user profile and posts
        loadUserProfile();
        loadUserPosts();
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this);
        binding.userPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.userPostsRecyclerView.setAdapter(postAdapter);
    }

    private void loadUserProfile() {
        binding.nameText.setText("Chargement...");
        binding.usernameText.setText("");
        binding.bioText.setText("");

        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    updateUI(user);
                } else {
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

        // Load profile image if available
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.profileImage);
        }

        // Load banner image if available
        if (user.getBannerImageUrl() != null && !user.getBannerImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getBannerImageUrl())
                    .placeholder(android.R.color.holo_blue_light)
                    .into(binding.coverImage);
        }
    }

    private void loadUserPosts() {
        // Query posts by this user
        Query query = postsRef.orderByChild("userId").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                binding.postsCount.setText(postCount + " Posts");
                
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
} 