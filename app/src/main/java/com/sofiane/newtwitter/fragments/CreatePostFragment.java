package com.sofiane.newtwitter.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.databinding.FragmentCreatePostBinding;
import com.sofiane.newtwitter.model.Post;

import java.util.Date;
import java.util.UUID;

public class CreatePostFragment extends Fragment {
    
    private FragmentCreatePostBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri selectedImageUri = null;
    
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.postImagePreview.setImageURI(uri);
                    binding.postImagePreview.setVisibility(View.VISIBLE);
                }
            });
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        binding.addImageButton.setOnClickListener(v -> 
            getContent.launch("image/*")
        );
        
        binding.postButton.setOnClickListener(v -> {
            String content = binding.postContentEdit.getText().toString().trim();
            
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_write_something), Toast.LENGTH_SHORT).show();
                return;
            }
            
            createPost(content);
        });
    }
    
    private void createPost(String content) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), getString(R.string.must_be_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.postProgress.setVisibility(View.VISIBLE);
        binding.postButton.setEnabled(false);
        
        String userId = auth.getCurrentUser().getUid();
        String username = auth.getCurrentUser().getDisplayName();
        String postId = UUID.randomUUID().toString();
        
        if (selectedImageUri != null) {
            // Upload image first
            StorageReference storageRef = storage.getReference().child("post_images/" + postId);
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> 
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> 
                            savePostToFirestore(postId, userId, username, content, downloadUri.toString())
                        )
                    )
                    .addOnFailureListener(e -> {
                        binding.postProgress.setVisibility(View.GONE);
                        binding.postButton.setEnabled(true);
                        Toast.makeText(requireContext(), getString(R.string.failed_to_upload_image, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image to upload
            savePostToFirestore(postId, userId, username, content, null);
        }
    }
    
    private void savePostToFirestore(String postId, String userId, String username, String content, String imageUrl) {
        Post post = new Post(
                postId,
                userId,
                username,
                content,
                imageUrl,
                new Date(),
                0
        );
        
        db.collection("posts")
                .document(postId)
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    binding.postProgress.setVisibility(View.GONE);
                    binding.postButton.setEnabled(true);
                    Toast.makeText(requireContext(), getString(R.string.post_created_successfully), Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    binding.postProgress.setVisibility(View.GONE);
                    binding.postButton.setEnabled(true);
                    Toast.makeText(requireContext(), getString(R.string.failed_to_create_post, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void clearForm() {
        binding.postContentEdit.setText("");
        binding.postImagePreview.setImageURI(null);
        binding.postImagePreview.setVisibility(View.GONE);
        selectedImageUri = null;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 