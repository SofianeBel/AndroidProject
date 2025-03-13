package com.sofiane.newtwitter.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.databinding.FragmentCreatePostBinding;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.model.User;
import com.sofiane.newtwitter.utils.ProfileIconHelper;
import com.sofiane.newtwitter.viewmodel.PostViewModel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Fragment permettant à l'utilisateur de créer un nouveau post (tweet) ou de répondre à un post existant.
 * Ce fragment gère la saisie du contenu du post, la sélection d'une image à joindre,
 * et l'envoi des données vers Firebase. Il peut fonctionner en mode création de post ou en mode réponse,
 * selon les arguments reçus.
 */
public class CreatePostFragment extends Fragment {
    private static final String TAG = "CreatePostFragment";
    private FragmentCreatePostBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private PostViewModel postViewModel;
    private Uri selectedImageUri;
    private boolean isReply = false;
    private Post parentPost;
    private User currentUserProfile;

    // ActivityResultLauncher pour la sélection d'images
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
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

        // Initialiser Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        userRef = database.getReference("users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialiser le ViewModel
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        // Initialiser l'ActivityResultLauncher pour la sélection d'images
        initializeImagePicker();

        // Vérifier si c'est une réponse à un post
        checkIfReply();

        // Charger les données de l'utilisateur courant
        loadCurrentUserProfile();

        // Configurer la toolbar
        setupToolbar();

        // Configurer les listeners pour les boutons
        setupButtonListeners();

        // Observer les messages d'erreur
        observeErrorMessages();
    }

    /**
     * Initialise l'ActivityResultLauncher pour la sélection d'images.
     */
    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            binding.selectedImage.setVisibility(View.VISIBLE);
                            binding.removeImageButton.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .centerCrop()
                                    .into(binding.selectedImage);
                        }
                    }
                });
    }

    /**
     * Vérifie si le fragment est utilisé pour répondre à un post existant.
     * Configure l'interface utilisateur en conséquence.
     */
    private void checkIfReply() {
        if (getArguments() != null && getArguments().containsKey("parent_post")) {
            isReply = true;
            parentPost = getArguments().getParcelable("parent_post");
            if (parentPost != null) {
                binding.replyToLayout.setVisibility(View.VISIBLE);
                binding.replyToUsername.setText("@" + parentPost.getUsername());
                binding.replyToContent.setText(parentPost.getContent());
            }
        } else {
            binding.replyToLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Charge les données du profil de l'utilisateur courant depuis Firebase Database.
     */
    private void loadCurrentUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserProfile = snapshot.getValue(User.class);
                if (currentUserProfile != null) {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erreur lors du chargement du profil: " + error.getMessage());
                Toast.makeText(requireContext(), "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Met à jour l'interface utilisateur avec les données du profil de l'utilisateur courant.
     */
    private void updateUI() {
        // Afficher le nom d'utilisateur
        binding.username.setText("@" + currentUserProfile.getUsername());

        // Charger l'image de profil
        if (currentUserProfile.getProfileImageUrl() != null && !currentUserProfile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUserProfile.getProfileImageUrl())
                    .circleCrop()
                    .into(binding.profileImage);
        } else {
            // Utiliser une image de profil par défaut basée sur l'ID utilisateur
            binding.profileImage.setImageDrawable(
                    ProfileIconHelper.getProfileIcon(requireContext(), currentUserProfile.getUserId())
            );
        }
    }

    /**
     * Configure la barre d'outils du fragment.
     * Définit le titre en fonction du mode (création ou réponse).
     */
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        if (isReply) {
            binding.toolbar.setTitle("Répondre");
        } else {
            binding.toolbar.setTitle("Nouveau tweet");
        }
    }

    /**
     * Configure les listeners pour les boutons et les zones cliquables.
     */
    private void setupButtonListeners() {
        // Bouton pour ajouter une image
        binding.addImageButton.setOnClickListener(v -> selectImage());

        // Bouton pour supprimer l'image sélectionnée
        binding.removeImageButton.setOnClickListener(v -> {
            selectedImageUri = null;
            binding.selectedImage.setVisibility(View.GONE);
            binding.removeImageButton.setVisibility(View.GONE);
        });

        // Bouton pour publier le post
        binding.postButton.setOnClickListener(v -> createPost());
    }

    /**
     * Observe les messages d'erreur du ViewModel et les affiche à l'utilisateur.
     */
    private void observeErrorMessages() {
        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Ouvre le sélecteur d'images pour choisir une image à joindre au post.
     */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Crée un nouveau post ou une réponse à un post existant.
     * Vérifie que le contenu n'est pas vide et gère le téléchargement de l'image si nécessaire.
     */
    private void createPost() {
        String content = binding.postContent.getText().toString().trim();

        // Vérifier que le contenu n'est pas vide
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Le contenu ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton de publication pendant le processus
        binding.postButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            // Si une image a été sélectionnée, la télécharger d'abord
            uploadImage(content);
        } else {
            // Sinon, créer directement le post sans image
            if (isReply && parentPost != null) {
                postViewModel.createReply(content, null, parentPost);
            } else {
                postViewModel.createPost(content, null);
            }
            // Revenir au fragment précédent
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    /**
     * Télécharge l'image sélectionnée vers Firebase Storage.
     *
     * @param content Le contenu textuel du post
     */
    private void uploadImage(String content) {
        // Convertir l'URI en Bitmap
        Bitmap bitmap = uriToBitmap(selectedImageUri);
        if (bitmap == null) {
            Toast.makeText(requireContext(), "Erreur lors du traitement de l'image", Toast.LENGTH_SHORT).show();
            binding.postButton.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        // Compresser l'image
        byte[] data = compressImage(bitmap);

        // Générer un nom unique pour l'image
        String imageName = "post_images/" + System.currentTimeMillis() + "_" + currentUser.getUid() + ".jpg";
        StorageReference imageRef = storageRef.child(imageName);

        // Télécharger l'image
        imageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> {
            // Obtenir l'URL de téléchargement
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Créer le post avec l'URL de l'image
                if (isReply && parentPost != null) {
                    postViewModel.createReply(content, uri.toString(), parentPost);
                } else {
                    postViewModel.createPost(content, uri.toString());
                }
                // Revenir au fragment précédent
                Navigation.findNavController(requireView()).navigateUp();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la récupération de l'URL de l'image", e);
                Toast.makeText(requireContext(), "Erreur lors de la publication", Toast.LENGTH_SHORT).show();
                binding.postButton.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erreur lors du téléchargement de l'image", e);
            Toast.makeText(requireContext(), "Erreur lors du téléchargement de l'image", Toast.LENGTH_SHORT).show();
            binding.postButton.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    /**
     * Compresse une image bitmap pour réduire sa taille avant téléchargement.
     *
     * @param bitmap L'image à compresser
     * @return Un tableau d'octets contenant l'image compressée
     */
    private byte[] compressImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return baos.toByteArray();
    }

    /**
     * Convertit un URI d'image en Bitmap.
     *
     * @param imageUri L'URI de l'image à convertir
     * @return Le Bitmap correspondant à l'image, ou null en cas d'erreur
     */
    private Bitmap uriToBitmap(Uri imageUri) {
        try {
            InputStream imageStream = requireContext().getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Fichier image non trouvé", e);
            return null;
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