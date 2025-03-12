package com.sofiane.newtwitter.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import com.google.firebase.storage.StorageException;

import android.graphics.Bitmap;
import android.graphics.ByteArrayOutputStream;

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
    private static final int PERMISSION_REQUEST_CODE = 1001;

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

        // Check permissions first
        checkAndRequestPermissions();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");
        
        try {
            storageRef = FirebaseStorage.getInstance().getReference();
            Log.d(TAG, "Firebase Storage initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Storage: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur d'initialisation du stockage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vous devez être connecté pour modifier votre profil", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Load user data
        loadUserProfile();

        // Set up click listeners
        binding.changeProfileImageButton.setOnClickListener(v -> {
            try {
                getProfileImage.launch("image/*");
            } catch (Exception e) {
                Log.e(TAG, "Error launching image picker: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Erreur lors de l'ouverture du sélecteur d'images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.changeBannerButton.setOnClickListener(v -> {
            try {
                getBannerImage.launch("image/*");
            } catch (Exception e) {
                Log.e(TAG, "Error launching image picker: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Erreur lors de l'ouverture du sélecteur d'images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // For Android 12 (S) and below
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            // For Android 13+ (TIRAMISU)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            Log.d(TAG, "Requesting permissions: " + permissionsNeeded);
            requestPermissions(permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "All required permissions already granted");
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Log.w(TAG, "Some permissions were denied");
                Toast.makeText(requireContext(), 
                        "L'accès aux images est nécessaire pour modifier votre profil", 
                        Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "All permissions granted");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

    // Interface pour gérer les callbacks d'upload d'images
    interface OnImageUploadListener {
        void onSuccess(String downloadUrl);
        void onFailure(String errorMessage);
    }

    private void saveProfile() {
        if (binding == null) {
            Log.e(TAG, "saveProfile: binding is null");
            return;
        }

        String username = binding.usernameEdit.getText().toString().trim();
        String bio = binding.bioEdit.getText().toString().trim();

        if (username.isEmpty()) {
            binding.usernameLayout.setError("Le nom d'utilisateur ne peut pas être vide");
            return;
        }

        // Disable button and show progress
        binding.saveProfileButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Update user profile
        userProfile.setUsername(username);
        userProfile.setBio(bio);

        // Update display name in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated in Firebase Auth");
                        
                        // Now handle image uploads
                        if (selectedProfileImageUri != null && selectedBannerImageUri != null) {
                            // Upload both images
                            uploadProfileImage(selectedProfileImageUri, false, new OnImageUploadListener() {
                                @Override
                                public void onSuccess(String downloadUrl) {
                                    userProfile.setProfileImageUrl(downloadUrl);
                                    uploadProfileImage(selectedBannerImageUri, true, new OnImageUploadListener() {
                                        @Override
                                        public void onSuccess(String downloadUrl) {
                                            userProfile.setBannerImageUrl(downloadUrl);
                                            saveUserToDatabase();
                                        }
                                        
                                        @Override
                                        public void onFailure(String errorMessage) {
                                            handleUploadError(errorMessage);
                                        }
                                    });
                                }
                                
                                @Override
                                public void onFailure(String errorMessage) {
                                    handleUploadError(errorMessage);
                                }
                            });
                        } else if (selectedProfileImageUri != null) {
                            // Upload only profile image
                            uploadProfileImage(selectedProfileImageUri, false, new OnImageUploadListener() {
                                @Override
                                public void onSuccess(String downloadUrl) {
                                    userProfile.setProfileImageUrl(downloadUrl);
                                    saveUserToDatabase();
                                }
                                
                                @Override
                                public void onFailure(String errorMessage) {
                                    handleUploadError(errorMessage);
                                }
                            });
                        } else if (selectedBannerImageUri != null) {
                            // Upload only banner image
                            uploadProfileImage(selectedBannerImageUri, true, new OnImageUploadListener() {
                                @Override
                                public void onSuccess(String downloadUrl) {
                                    userProfile.setBannerImageUrl(downloadUrl);
                                    saveUserToDatabase();
                                }
                                
                                @Override
                                public void onFailure(String errorMessage) {
                                    handleUploadError(errorMessage);
                                }
                            });
                        } else {
                            // No images to upload, just save user data
                            saveUserToDatabase();
                        }
                    } else {
                        Log.e(TAG, "Failed to update user profile in Firebase Auth", task.getException());
                        binding.progressBar.setVisibility(View.GONE);
                        binding.saveProfileButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Erreur lors de la mise à jour du profil: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Erreur inconnue"), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void handleUploadError(String errorMessage) {
        Log.e(TAG, "Image upload error: " + errorMessage);
        binding.progressBar.setVisibility(View.GONE);
        binding.saveProfileButton.setEnabled(true);
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }
    
    private void saveUserToDatabase() {
        Log.d(TAG, "Saving user to database: " + userProfile.toString());
        
        usersRef.child(currentUser.getUid()).setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data: " + e.getMessage(), e);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Erreur lors de la sauvegarde des données: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadProfileImage(Uri imageUri, boolean isBanner, OnImageUploadListener listener) {
        if (imageUri == null) {
            Log.e(TAG, "uploadProfileImage: imageUri is null");
            listener.onFailure("Aucune image sélectionnée");
            return;
        }

        if (storageRef == null) {
            try {
                storageRef = FirebaseStorage.getInstance().getReference();
                Log.d(TAG, "Storage reference initialized");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize storage reference: " + e.getMessage(), e);
                listener.onFailure("Erreur d'initialisation du stockage: " + e.getMessage());
                return;
            }
        }

        if (currentUser == null || currentUser.getUid() == null) {
            Log.e(TAG, "uploadProfileImage: currentUser or UID is null");
            listener.onFailure("Utilisateur non connecté");
            return;
        }

        try {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            String path = isBanner ? "banner_images" : "profile_images";
            String fullPath = path + "/" + currentUser.getUid() + "/" + fileName;
            
            Log.d(TAG, "Uploading " + (isBanner ? "banner" : "profile") + " image to: " + fullPath);
            
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(fullPath);
            
            // Show progress
            binding.progressBar.setVisibility(View.VISIBLE);
            
            // Compression de l'image avant l'upload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            bitmap = getResizedBitmap(bitmap, isBanner ? 1200 : 800); // Redimensionner selon le type d'image
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            
            // Upload des données compressées
            UploadTask uploadTask = fileRef.putBytes(data);
            uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image uploaded successfully to: " + fullPath);
                    fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d(TAG, "Download URL retrieved: " + uri.toString());
                            binding.progressBar.setVisibility(View.GONE);
                            listener.onSuccess(uri.toString());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get download URL: " + e.getMessage(), e);
                            binding.progressBar.setVisibility(View.GONE);
                            listener.onFailure("Erreur lors de la récupération de l'URL: " + e.getMessage());
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload image: " + e.getMessage(), e);
                    binding.progressBar.setVisibility(View.GONE);
                    
                    String errorMessage;
                    if (e instanceof StorageException) {
                        StorageException storageException = (StorageException) e;
                        int errorCode = storageException.getErrorCode();
                        
                        switch (errorCode) {
                            case StorageException.ERROR_QUOTA_EXCEEDED:
                                errorMessage = "Quota de stockage dépassé";
                                break;
                            case StorageException.ERROR_NOT_AUTHENTICATED:
                                errorMessage = "Utilisateur non authentifié";
                                break;
                            case StorageException.ERROR_NOT_AUTHORIZED:
                                errorMessage = "Opération non autorisée";
                                break;
                            case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                                errorMessage = "Limite de tentatives dépassée";
                                break;
                            case StorageException.ERROR_OBJECT_NOT_FOUND:
                                errorMessage = "Erreur: Chemin de stockage non trouvé";
                                break;
                            default:
                                errorMessage = "Erreur de téléchargement: " + e.getMessage();
                        }
                    } else {
                        errorMessage = "Erreur de téléchargement: " + e.getMessage();
                    }
                    
                    listener.onFailure(errorMessage);
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload progress: " + progress + "%");
                });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during upload: " + e.getMessage(), e);
            binding.progressBar.setVisibility(View.GONE);
            listener.onFailure("Erreur inattendue: " + e.getMessage());
        }
    }

    // Méthode pour redimensionner les images avant upload
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            // Image plus large que haute
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            // Image plus haute que large
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 