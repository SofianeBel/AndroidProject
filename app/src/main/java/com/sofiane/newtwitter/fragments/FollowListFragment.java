package com.sofiane.newtwitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.adapter.UserAdapter;
import com.sofiane.newtwitter.databinding.FragmentFollowListBinding;
import com.sofiane.newtwitter.model.User;
import com.sofiane.newtwitter.utils.FollowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment affichant une liste d'utilisateurs suivis ou de followers.
 * Ce fragment présente soit la liste des utilisateurs qui suivent un utilisateur spécifique (followers),
 * soit la liste des utilisateurs suivis par un utilisateur spécifique (following).
 * Il permet également de naviguer vers les profils des utilisateurs affichés.
 */
public class FollowListFragment extends Fragment implements UserAdapter.OnUserInteractionListener {
    private static final String TAG = "FollowListFragment";
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_LIST_TYPE = "listType";
    
    public static final int TYPE_FOLLOWERS = 0;
    public static final int TYPE_FOLLOWING = 1;
    
    private FragmentFollowListBinding binding;
    private UserAdapter adapter;
    private FollowManager followManager;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private DatabaseReference followsRef;
    
    private String userId;
    private int listType;
    private String username;
    
    public static FollowListFragment newInstance(String userId, int listType) {
        FollowListFragment fragment = new FollowListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putInt(ARG_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            listType = getArguments().getInt(ARG_LIST_TYPE, TYPE_FOLLOWERS);
        }
        
        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");
        followsRef = database.getReference("follows");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        followManager = new FollowManager();
    }
    
    /**
     * Crée et retourne la vue associée au fragment.
     *
     * @param inflater L'inflater utilisé pour gonfler la vue
     * @param container Le conteneur parent
     * @param savedInstanceState L'état sauvegardé du fragment
     * @return La vue racine du fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    /**
     * Initialise les composants de l'interface utilisateur et configure les observateurs
     * après que la vue a été créée.
     *
     * @param view La vue racine du fragment
     * @param savedInstanceState L'état sauvegardé du fragment
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up RecyclerView
        adapter = new UserAdapter(requireContext(), this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        
        // Set up toolbar
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        // Load username
        loadUsername();
        
        // Load users
        loadUsers();
    }
    
    private void loadUsername() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    username = user.getUsername();
                    updateTitle();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading username: " + error.getMessage());
            }
        });
    }
    
    /**
     * Configure la barre d'outils du fragment.
     * Définit le titre en fonction du type de liste (followers ou following).
     */
    private void updateTitle() {
        if (username != null) {
            String title;
            if (listType == TYPE_FOLLOWERS) {
                title = getString(R.string.followers_list, username);
            } else {
                title = getString(R.string.following_list, username);
            }
            binding.toolbarTitle.setText(title);
        } else {
            binding.toolbarTitle.setText(listType == TYPE_FOLLOWERS ? R.string.followers : R.string.following);
        }
    }
    
    /**
     * Charge la liste des utilisateurs en fonction du type de liste (followers ou following).
     */
    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        
        String childPath = listType == TYPE_FOLLOWERS ? "followers" : "following";
        
        followsRef.child(userId).child(childPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    showEmptyView();
                    return;
                }
                
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String followUserId = userSnapshot.getKey();
                    if (followUserId != null) {
                        userIds.add(followUserId);
                    }
                }
                
                if (userIds.isEmpty()) {
                    showEmptyView();
                } else {
                    loadUserDetails(userIds);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading follow list: " + error.getMessage());
                Toast.makeText(requireContext(), R.string.error_loading_users, Toast.LENGTH_SHORT).show();
                showEmptyView();
            }
        });
    }
    
    /**
     * Charge les détails des utilisateurs à partir de leurs identifiants.
     *
     * @param userIds Liste des identifiants des utilisateurs à charger
     */
    private void loadUserDetails(List<String> userIds) {
        List<User> users = new ArrayList<>();
        final int[] loadedCount = {0};
        
        for (String userId : userIds) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    loadedCount[0]++;
                    
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                    
                    // Check if all users are loaded
                    if (loadedCount[0] >= userIds.size()) {
                        if (users.isEmpty()) {
                            showEmptyView();
                        } else {
                            adapter.setUsers(users);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.emptyView.setVisibility(View.GONE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadedCount[0]++;
                    Log.e(TAG, "Error loading user details: " + error.getMessage());
                    
                    // Check if all users are loaded
                    if (loadedCount[0] >= userIds.size()) {
                        if (users.isEmpty()) {
                            showEmptyView();
                        } else {
                            adapter.setUsers(users);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.emptyView.setVisibility(View.GONE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }
    
    private void showEmptyView() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.emptyView.setText(listType == TYPE_FOLLOWERS ? R.string.no_followers : R.string.no_following);
    }
    
    /**
     * Appelé lorsque l'utilisateur clique sur un profil utilisateur.
     * Navigue vers le fragment de profil de l'utilisateur.
     *
     * @param user L'utilisateur cliqué
     */
    @Override
    public void onUserClicked(User user) {
        // Navigate to user profile
        Bundle args = new Bundle();
        args.putString("userId", user.getUserId());
        Navigation.findNavController(requireView()).navigate(R.id.action_followListFragment_to_profileFragment, args);
    }
    
    @Override
    public void onFollowClicked(User user, boolean isFollowing) {
        if (isFollowing) {
            followManager.unfollowUser(user.getUserId(), new FollowManager.FollowListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), R.string.unfollow_success, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), R.string.unfollow_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            followManager.followUser(user.getUserId(), new FollowManager.FollowListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), R.string.follow_success, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), R.string.follow_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Nettoie les ressources lorsque la vue est détruite.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 