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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.sofiane.newtwitter.utils.ProfileIconHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import com.google.firebase.storage.StorageException;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import android.graphics.BitmapFactory;

/**
 * Fragment permettant à l'utilisateur de modifier son profil.
 * Ce fragment offre des fonctionnalités pour changer la photo de profil, la bannière,
 * le nom d'utilisateur, la biographie et d'autres informations personnelles.
 * Il gère également le téléchargement des images vers Firebase Storage et la mise à jour
 * des données utilisateur dans Firebase Database.
 */
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
    
    // Variables pour la sélection d'icône et de couleur
    private int selectedIconIndex = 0;
    private int selectedColorIndex = 0;

    // Commenté car nous n'utilisons plus Firebase Storage pour les images
    /*
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
    */

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
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
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

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");
        
        // Commenté car nous n'utilisons plus Firebase Storage pour les images
        /*
        try {
            storageRef = FirebaseStorage.getInstance().getReference();
            Log.d(TAG, "Firebase Storage initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Storage: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Erreur d'initialisation du stockage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        */

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Vous devez être connecté pour modifier votre profil", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Initialiser les spinners pour les icônes et les couleurs
        setupIconSpinner();
        setupColorSpinner();
        
        // Commenté car nous n'utilisons plus Firebase Storage pour les images
        /*
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
        */
        
        // Mettre à jour l'aperçu de l'icône lorsque l'utilisateur change l'icône ou la couleur
        binding.profileIconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Icône sélectionnée: position=" + position);
                selectedIconIndex = position;
                updateProfileIconPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });
        
        binding.profileColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Couleur sélectionnée: position=" + position);
                selectedColorIndex = position;
                updateProfileIconPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });
        
        binding.saveProfileButton.setOnClickListener(v -> saveProfile());

        // Load user profile
        loadUserProfile();
    }
    
    /**
     * Configure le spinner pour les icônes de profil
     */
    private void setupIconSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.profile_icon_names,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.profileIconSpinner.setAdapter(adapter);
    }
    
    /**
     * Configure le spinner pour les couleurs de profil
     */
    private void setupColorSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.profile_colors,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.profileColorSpinner.setAdapter(adapter);
    }
    
    /**
     * Met à jour l'aperçu de l'icône de profil avec l'icône et la couleur sélectionnées
     */
    private void updateProfileIconPreview() {
        Log.d(TAG, "updateProfileIconPreview: selectedIconIndex=" + selectedIconIndex + ", selectedColorIndex=" + selectedColorIndex);
        Drawable coloredIcon = ProfileIconHelper.getColoredProfileIcon(
                requireContext(),
                selectedIconIndex,
                selectedColorIndex
        );
        binding.profileImagePreview.setImageDrawable(coloredIcon);
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                
                // Vérifier si les données existent
                if (snapshot.exists()) {
                    try {
                        userProfile = snapshot.getValue(User.class);
                        
                        // Vérifier si l'objet utilisateur est valide
                        if (userProfile != null) {
                            Log.d(TAG, "Profil utilisateur chargé avec succès: " + userProfile.getUsername());
                            Log.d(TAG, "Index chargés: iconIndex=" + userProfile.getProfileIconIndex() + ", colorIndex=" + userProfile.getProfileColorIndex());
                            
                            // Remplir le formulaire avec les données utilisateur
                            binding.usernameEdit.setText(userProfile.getUsername());
                            binding.bioEdit.setText(userProfile.getBio());
                            
                            // Définir les sélections d'icône et de couleur
                            binding.profileIconSpinner.setSelection(userProfile.getProfileIconIndex());
                            binding.profileColorSpinner.setSelection(userProfile.getProfileColorIndex());
                            
                            // Mettre à jour l'aperçu de l'icône
                            selectedIconIndex = userProfile.getProfileIconIndex();
                            selectedColorIndex = userProfile.getProfileColorIndex();
                            Log.d(TAG, "Variables locales mises à jour: selectedIconIndex=" + selectedIconIndex + ", selectedColorIndex=" + selectedColorIndex);
                            updateProfileIconPreview();
                            
                            return; // Sortir de la méthode car tout est OK
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la conversion des données utilisateur: " + e.getMessage(), e);
                        // Continuer pour créer un nouveau profil
                    }
                }
                
                // Si on arrive ici, c'est que les données n'existent pas ou sont invalides
                Log.w(TAG, "Aucun profil utilisateur trouvé ou données invalides, création d'un nouveau profil");
                
                // Créer un nouveau profil utilisateur avec les valeurs par défaut
                String displayName = currentUser.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = "Utilisateur"; // Valeur par défaut
                }
                
                userProfile = new User(
                    currentUser.getUid(),
                    displayName,
                    currentUser.getEmail()
                );
                
                // Remplir le formulaire avec les données par défaut
                binding.usernameEdit.setText(userProfile.getUsername());
                if (userProfile.getBio() != null) {
                    binding.bioEdit.setText(userProfile.getBio());
                }
                
                // Définir les sélections d'icône et de couleur par défaut
                binding.profileIconSpinner.setSelection(0);
                binding.profileColorSpinner.setSelection(0);
                selectedIconIndex = 0;
                selectedColorIndex = 0;
                Log.d(TAG, "Valeurs par défaut définies: selectedIconIndex=" + selectedIconIndex + ", selectedColorIndex=" + selectedColorIndex);
                updateProfileIconPreview();
                
                // Sauvegarder immédiatement ce nouveau profil dans la base de données
                saveUserToDatabase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Erreur lors du chargement du profil: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading user profile: " + error.getMessage());
            }
        });
    }

    interface OnImageUploadListener {
        void onSuccess(String downloadUrl);
        void onFailure(String errorMessage);
    }

    private void saveProfile() {
        if (binding == null) {
            Log.e(TAG, "saveProfile: binding is null");
            return;
        }

        // Get user input
        String username = binding.usernameEdit.getText().toString().trim();
        String bio = binding.bioEdit.getText().toString().trim();

        // Validate input
        if (username.isEmpty()) {
            binding.usernameLayout.setError("Le nom d'utilisateur est requis");
            return;
        }

        // Log les valeurs des index avant la mise à jour
        Log.d(TAG, "saveProfile: avant mise à jour - selectedIconIndex=" + selectedIconIndex + ", selectedColorIndex=" + selectedColorIndex);

        // Disable button and show progress
        binding.saveProfileButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Vérifier si userProfile est null
        if (userProfile == null) {
            Log.e(TAG, "saveProfile: userProfile is null, creating new user");
            userProfile = new User(
                currentUser.getUid(),
                username,
                currentUser.getEmail()
            );
        }

        // Update user profile
        userProfile.setUsername(username);
        userProfile.setBio(bio);
        userProfile.setProfileIconIndex(selectedIconIndex);
        userProfile.setProfileColorIndex(selectedColorIndex);
        
        // S'assurer que l'ID est correctement défini
        userProfile.setId(currentUser.getUid());
        userProfile.setUserId(currentUser.getUid());

        // Log les valeurs des index après la mise à jour de l'objet userProfile
        Log.d(TAG, "saveProfile: après mise à jour de userProfile - iconIndex=" + userProfile.getProfileIconIndex() + ", colorIndex=" + userProfile.getProfileColorIndex());

        // Update display name in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated in Firebase Auth");
                        
                        // Sauvegarder directement les données utilisateur
                        saveUserToDatabase();
                        
                        // Commenté car nous n'utilisons plus Firebase Storage pour les images
                        /*
                        // Now handle image uploads
                        if (selectedProfileImageUri != null && selectedBannerImageUri != null) {
                            // Both profile and banner images selected
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
                            // Only profile image selected
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
                            // Only banner image selected
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
                            // No images selected, just save user data
                            saveUserToDatabase();
                        }
                        */
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
        if (userProfile == null) {
            Log.e(TAG, "saveUserToDatabase: userProfile is null");
            binding.progressBar.setVisibility(View.GONE);
            binding.saveProfileButton.setEnabled(true);
            Toast.makeText(requireContext(), "Erreur: profil utilisateur non disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Saving user to database: " + userProfile.toString());
        Log.d(TAG, "saveUserToDatabase: iconIndex=" + userProfile.getProfileIconIndex() + ", colorIndex=" + userProfile.getProfileColorIndex());
        
        // S'assurer que l'ID est correctement défini avant la sauvegarde
        userProfile.setId(currentUser.getUid());
        userProfile.setUserId(currentUser.getUid());
        
        usersRef.child(currentUser.getUid()).setValue(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    Log.d(TAG, "Après sauvegarde: iconIndex=" + userProfile.getProfileIconIndex() + ", colorIndex=" + userProfile.getProfileColorIndex());
                    
                    // Vérifier que les données ont bien été sauvegardées en les relisant
                    usersRef.child(currentUser.getUid()).get().addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            User savedUser = dataSnapshot.getValue(User.class);
                            if (savedUser != null) {
                                Log.d(TAG, "Données relues après sauvegarde: iconIndex=" + savedUser.getProfileIconIndex() + 
                                        ", colorIndex=" + savedUser.getProfileColorIndex());
                            }
                        }
                    });
                    
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

    // Commenté car nous n'utilisons plus Firebase Storage pour les images
    /*
    private void uploadProfileImage(Uri imageUri, boolean isBanner, OnImageUploadListener listener) {
        if (imageUri == null) {
            Log.e(TAG, "uploadProfileImage: imageUri is null");
            listener.onFailure("Aucune image sélectionnée");
            return;
        }

        if (currentUser == null || currentUser.getUid() == null) {
            Log.e(TAG, "uploadProfileImage: currentUser or UID is null");
            listener.onFailure("Utilisateur non connecté");
            return;
        }

        try {
            // Initialiser la référence de stockage si nécessaire
            if (storageRef == null) {
                storageRef = FirebaseStorage.getInstance().getReference();
                Log.d(TAG, "Storage reference initialized");
            }
            
            // Créer un nom de fichier unique avec extension
            String userId = currentUser.getUid();
            String fileName = UUID.randomUUID().toString() + ".jpg";
            String folderPath = isBanner ? "banner_images" : "profile_images";
            
            // Créer la référence de stockage directement
            StorageReference fileRef = FirebaseStorage.getInstance()
                .getReference()
                .child(folderPath)
                .child(userId)
                .child(fileName);
            
            Log.d(TAG, "Uploading " + (isBanner ? "banner" : "profile") + " image to path: " + fileRef.getPath());
            
            // Afficher la progression
            binding.progressBar.setVisibility(View.VISIBLE);
            
            try {
                // Compression de l'image avant l'upload
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                bitmap = getResizedBitmap(bitmap, isBanner ? 1200 : 800);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] data = baos.toByteArray();
                
                // Upload des données compressées
                UploadTask uploadTask = fileRef.putBytes(data);
                
                // Attendre que l'upload soit terminé avant de récupérer l'URL
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Upload failed: " + task.getException());
                        throw task.getException();
                    }
                    
                    // L'upload est terminé, récupérer l'URL
                    Log.d(TAG, "Upload completed, getting download URL");
                    return fileRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.d(TAG, "Download URL retrieved: " + downloadUri.toString());
                        listener.onSuccess(downloadUri.toString());
                    } else {
                        Exception e = task.getException();
                        Log.e(TAG, "Failed to get download URL: " + (e != null ? e.getMessage() : "Unknown error"), e);
                        
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
                                    errorMessage = "Erreur de stockage: " + storageException.getMessage();
                                    break;
                            }
                        } else {
                            errorMessage = "Erreur lors de l'upload: " + (e != null ? e.getMessage() : "Erreur inconnue");
                        }
                        
                        listener.onFailure(errorMessage);
                    }
                });
                
                // Ajouter un écouteur de progression
                uploadTask.addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "Upload progress: " + progress + "%");
                });
                
            } catch (IOException e) {
                Log.e(TAG, "Error processing image: " + e.getMessage(), e);
                binding.progressBar.setVisibility(View.GONE);
                listener.onFailure("Erreur lors du traitement de l'image: " + e.getMessage());
            }
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
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 