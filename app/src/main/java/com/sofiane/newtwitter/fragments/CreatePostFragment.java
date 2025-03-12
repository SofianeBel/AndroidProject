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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.databinding.FragmentCreatePostBinding;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.viewmodel.PostViewModel;

import java.util.Date;
import java.util.UUID;

public class CreatePostFragment extends Fragment {
    
    private FragmentCreatePostBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference postsRef;
    private FirebaseStorage storage;
    private Uri selectedImageUri = null;
    private PostViewModel postViewModel;
    
    // Variables pour les réponses
    private String parentPostId;
    private String parentUsername;
    private boolean isReply = false;
    
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
        database = FirebaseDatabase.getInstance();
        postsRef = database.getReference("posts");
        storage = FirebaseStorage.getInstance();
        
        // Initialize ViewModel
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        
        // Récupérer les arguments pour les réponses
        Bundle args = getArguments();
        if (args != null) {
            parentPostId = args.getString("parent_post_id");
            parentUsername = args.getString("parent_username");
            isReply = parentPostId != null && !parentPostId.isEmpty();
        }
        
        // Configurer l'interface pour les réponses
        if (isReply) {
            binding.titleText.setText(R.string.reply_to_post);
            binding.replyingToLayout.setVisibility(View.VISIBLE);
            binding.replyingToTextView.setText(getString(R.string.replying_to, parentUsername));
            binding.postButton.setText(R.string.reply);
            binding.postContentEdit.setHint(R.string.write_your_reply);
        }
        
        // Observe error messages
        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                binding.postProgress.setVisibility(View.GONE);
                binding.postButton.setEnabled(true);
            }
        });
        
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
            
            if (isReply) {
                createReply(content);
            } else {
                createPost(content);
            }
        });
    }
    
    private void createReply(String content) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), getString(R.string.must_be_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.postProgress.setVisibility(View.VISIBLE);
        binding.postButton.setEnabled(false);
        
        if (selectedImageUri != null) {
            // Upload image first
            String replyId = UUID.randomUUID().toString();
            StorageReference storageRef = storage.getReference().child("post_images/" + replyId);
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> 
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            // Create reply with image URL
                            createReplyWithImage(content, downloadUri.toString());
                            clearForm();
                            navigateBack();
                        })
                    )
                    .addOnFailureListener(e -> {
                        binding.postProgress.setVisibility(View.GONE);
                        binding.postButton.setEnabled(true);
                        Toast.makeText(requireContext(), getString(R.string.failed_to_upload_image, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image to upload, create reply directly
            postViewModel.createReply(content, parentPostId);
            binding.postProgress.setVisibility(View.GONE);
            binding.postButton.setEnabled(true);
            Toast.makeText(requireContext(), getString(R.string.reply_created_successfully), Toast.LENGTH_SHORT).show();
            clearForm();
            navigateBack();
        }
    }
    
    private void createReplyWithImage(String content, String imageUrl) {
        String userId = auth.getCurrentUser().getUid();
        String username = auth.getCurrentUser().getDisplayName();
        if (username == null || username.isEmpty()) {
            username = "User" + userId.substring(0, 5);
        }
        
        // Generate a unique key for the new reply
        String replyId = postsRef.push().getKey();
        if (replyId == null) {
            Toast.makeText(requireContext(), "Failed to create reply ID", Toast.LENGTH_SHORT).show();
            binding.postProgress.setVisibility(View.GONE);
            binding.postButton.setEnabled(true);
            return;
        }
        
        // Create reply object
        Post reply = new Post(
            replyId,
            userId,
            username,
            content,
            imageUrl,
            new Date(),
            0, // Initial like count
            parentPostId // Parent post ID
        );
        
        // Save reply to Firebase
        postsRef.child(replyId).setValue(reply)
            .addOnSuccessListener(aVoid -> {
                binding.postProgress.setVisibility(View.GONE);
                binding.postButton.setEnabled(true);
                Toast.makeText(requireContext(), getString(R.string.reply_created_successfully), Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                binding.postProgress.setVisibility(View.GONE);
                binding.postButton.setEnabled(true);
                Toast.makeText(requireContext(), getString(R.string.failed_to_create_reply, e.getMessage()), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createPost(String content) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), getString(R.string.must_be_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.postProgress.setVisibility(View.VISIBLE);
        binding.postButton.setEnabled(false);
        
        if (selectedImageUri != null) {
            // Upload image first
            String postId = UUID.randomUUID().toString();
            StorageReference storageRef = storage.getReference().child("post_images/" + postId);
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> 
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            // Create post with image URL
                            createPostWithImage(content, downloadUri.toString());
                            clearForm();
                            navigateBack();
                        })
                    )
                    .addOnFailureListener(e -> {
                        binding.postProgress.setVisibility(View.GONE);
                        binding.postButton.setEnabled(true);
                        Toast.makeText(requireContext(), getString(R.string.failed_to_upload_image, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image to upload, create post directly
            postViewModel.createPost(content);
            binding.postProgress.setVisibility(View.GONE);
            binding.postButton.setEnabled(true);
            Toast.makeText(requireContext(), getString(R.string.post_created_successfully), Toast.LENGTH_SHORT).show();
            clearForm();
            navigateBack();
        }
    }
    
    private void createPostWithImage(String content, String imageUrl) {
        String userId = auth.getCurrentUser().getUid();
        String username = auth.getCurrentUser().getDisplayName();
        if (username == null || username.isEmpty()) {
            username = "User" + userId.substring(0, 5);
        }
        
        // Generate a unique key for the new post
        String postId = postsRef.push().getKey();
        if (postId == null) {
            Toast.makeText(requireContext(), "Failed to create post ID", Toast.LENGTH_SHORT).show();
            binding.postProgress.setVisibility(View.GONE);
            binding.postButton.setEnabled(true);
            return;
        }
        
        // Create post object
        Post post = new Post(
            postId,
            userId,
            username,
            content,
            imageUrl,
            new Date(),
            0 // Initial like count
        );
        
        // Save post to Firebase
        postsRef.child(postId).setValue(post)
            .addOnSuccessListener(aVoid -> {
                binding.postProgress.setVisibility(View.GONE);
                binding.postButton.setEnabled(true);
                Toast.makeText(requireContext(), getString(R.string.post_created_successfully), Toast.LENGTH_SHORT).show();
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
    
    private void navigateBack() {
        Navigation.findNavController(requireView()).navigateUp();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 