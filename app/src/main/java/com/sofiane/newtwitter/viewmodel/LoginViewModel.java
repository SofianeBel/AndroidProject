package com.sofiane.newtwitter.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.sofiane.newtwitter.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

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
        errorMessage.setValue(null);
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Vérifier si l'utilisateur existe dans la Realtime Database
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
                    database.getReference("users").child(firebaseUser.getUid()).get()
                        .addOnSuccessListener(dataSnapshot -> {
                            if (dataSnapshot.exists()) {
                                // L'utilisateur existe déjà dans la Realtime Database
                                User user = dataSnapshot.getValue(User.class);
                                currentUser.setValue(user);
                            } else {
                                // L'utilisateur n'existe pas dans la Realtime Database, le créer
                                User user = new User(firebaseUser.getUid(), 
                                    firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User", 
                                    firebaseUser.getEmail());
                                
                                database.getReference("users").child(firebaseUser.getUid()).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        currentUser.setValue(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to create user in Realtime Database: " + e.getMessage());
                                        errorMessage.setValue("Failed to create user profile: " + e.getMessage());
                                    });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to check user in Realtime Database: " + e.getMessage());
                            // Créer quand même un objet User avec les données de base
                            User user = new User(firebaseUser.getUid(), 
                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User", 
                                firebaseUser.getEmail());
                            currentUser.setValue(user);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Login failed: " + e.getMessage());
                errorMessage.setValue("Login failed: " + e.getMessage());
            });
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