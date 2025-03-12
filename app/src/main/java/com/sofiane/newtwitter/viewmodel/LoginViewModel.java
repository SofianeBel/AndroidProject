package com.sofiane.newtwitter.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.sofiane.newtwitter.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final FirebaseAuth auth;

    public LoginViewModel() {
        auth = FirebaseAuth.getInstance();
        
        // Commenté pour éviter la connexion automatique qui cause le crash
        /*
        // Check if user is already signed in
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            Log.d(TAG, "User already signed in: " + firebaseUser.getEmail());
            currentUser.setValue(new User(firebaseUser.getUid(), 
                firebaseUser.getDisplayName(), 
                firebaseUser.getEmail()));
        }
        */
    }

    public void login(String email, String password) {
        Log.d(TAG, "Attempting login for email: " + email);
        
        // Clear any previous error messages
        errorMessage.setValue(null);
        
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Login successful");
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        // Vérifier si l'utilisateur a un displayName
                        String displayName = firebaseUser.getDisplayName();
                        if (displayName == null || displayName.isEmpty()) {
                            displayName = "User"; // Valeur par défaut
                            Log.w(TAG, "User has no display name, using default");
                        }
                        
                        User user = new User(
                            firebaseUser.getUid(), 
                            displayName, 
                            firebaseUser.getEmail()
                        );
                        Log.d(TAG, "Setting user to LiveData: " + user.getEmail() + ", UID: " + user.getUserId());
                        
                        // Utiliser postValue pour éviter les problèmes de thread
                        currentUser.postValue(user);
                        Log.d(TAG, "User data set to LiveData");
                    } else {
                        Log.e(TAG, "FirebaseUser is null after successful login");
                        errorMessage.postValue("Authentication error: User data not available");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failed: " + e.getMessage(), e);
                    errorMessage.postValue(e.getMessage());
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception during login process: " + e.getMessage(), e);
            errorMessage.postValue("Login error: " + e.getMessage());
        }
    }

    public void logout() {
        Log.d(TAG, "Logging out user");
        auth.signOut();
        currentUser.setValue(null);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void register(String username, String email, String password) {
        Log.d(TAG, "Starting registration process for email: " + email);
        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                Log.d(TAG, "Firebase Auth account created successfully");
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Update user profile with username
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build();

                    firebaseUser.updateProfile(profileUpdates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Firebase user profile updated successfully");
                            // User created and profile updated successfully
                            User newUser = new User(firebaseUser.getUid(), username, email);
                            currentUser.setValue(newUser);
                            Log.d(TAG, "currentUser LiveData updated with new user");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update profile: " + e.getMessage());
                            errorMessage.setValue("Failed to update profile: " + e.getMessage());
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Registration failed: " + e.getMessage());
                errorMessage.setValue("Registration failed: " + e.getMessage());
            });
    }
} 