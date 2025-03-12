package com.sofiane.newtwitter.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.databinding.FragmentEditProfileBinding;
import com.sofiane.newtwitter.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private FragmentEditProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private StorageReference storageRef;
    private Uri selectedProfileImageUri = null;
    private Uri selectedBannerImageUri = null;
    private User userProfile;

    private final ActivityResultLauncher<String> getProfileImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedProfileImageUri = uri;
                    binding.profileImagePreview.setImageURI(uri);
                    binding.changeProfileImageButton.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<String> getBannerImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedBannerImageUri = uri;
                    binding.bannerImagePreview.setImageURI(uri);
                    binding.changeBannerButton.setVisibility(View.GONE);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
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
        storageRef = FirebaseStorage.getInstance().getReference();

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vous devez être connecté pour modifier votre profil", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Load user data
        loadUserProfile();

        // Set up click listeners
        binding.changeProfileImageButton.setOnClickListener(v -> getProfileImage.launch("image/*"));
        binding.changeBannerButton.setOnClickListener(v -> getBannerImage.launch("image/*"));
        binding.saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
                    // Fill form with user data
                    binding.usernameEdit.setText(userProfile.getUsername());
                    binding.bioEdit.setText(userProfile.getBio());

                    // Load profile image if exists
                    if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
                        Glide.with(requireContext())
                                .load(userProfile.getProfileImageUrl())
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(binding.profileImagePreview);
                    }

                    // Load banner image if exists
                    if (userProfile.getBannerImageUrl() != null && !userProfile.getBannerImageUrl().isEmpty()) {
                        Glide.with(requireContext())
                                .load(userProfile.getBannerImageUrl())
                                .placeholder(android.R.color.holo_blue_light)
                                .into(binding.bannerImagePreview);
                    }
                } else {
                    // Create new user profile if it doesn't exist
                    userProfile = new User(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());
                    binding.usernameEdit.setText(userProfile.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Erreur lors du chargement du profil: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading user profile: " + error.getMessage());
            }
        });
    }

    private void saveProfile() {
        String username = binding.usernameEdit.getText().toString().trim();
        String bio = binding.bioEdit.getText().toString().trim();

        if (username.isEmpty()) {
            binding.usernameLayout.setError("Le nom d'utilisateur ne peut pas être vide");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveProfileButton.setEnabled(false);

        // Update user profile in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Update user data in database
                    updateUserData(username, bio);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Erreur lors de la mise à jour du profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating profile: " + e.getMessage());
                });
    }

    private void updateUserData(String username, String bio) {
        // Update user object
        userProfile.setUsername(username);
        userProfile.setBio(bio);

        // Handle image uploads
        if (selectedProfileImageUri != null && selectedBannerImageUri != null) {
            // Upload both images
            uploadProfileImage(() -> uploadBannerImage(() -> saveUserToDatabase()));
        } else if (selectedProfileImageUri != null) {
            // Upload only profile image
            uploadProfileImage(() -> saveUserToDatabase());
        } else if (selectedBannerImageUri != null) {
            // Upload only banner image
            uploadBannerImage(() -> saveUserToDatabase());
        } else {
            // No images to upload, just save user data
            saveUserToDatabase();
        }
    }

    private void uploadProfileImage(Runnable onComplete) {
        String imageFileName = "profile_" + currentUser.getUid() + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference profileImageRef = storageRef.child("profile_images/" + imageFileName);

        profileImageRef.putFile(selectedProfileImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userProfile.setProfileImageUrl(uri.toString());
                        onComplete.run();
                    });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Erreur lors de l'upload de l'image de profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error uploading profile image: " + e.getMessage());
                });
    }

    private void uploadBannerImage(Runnable onComplete) {
        String imageFileName = "banner_" + currentUser.getUid() + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference bannerImageRef = storageRef.child("banner_images/" + imageFileName);

        bannerImageRef.putFile(selectedBannerImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    bannerImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userProfile.setBannerImageUrl(uri.toString());
                        onComplete.run();
                    });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Erreur lors de l'upload de la bannière: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error uploading banner image: " + e.getMessage());
                });
    }

    private void saveUserToDatabase() {
        usersRef.child(currentUser.getUid()).setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Erreur lors de la sauvegarde du profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving user to database: " + e.getMessage());
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 