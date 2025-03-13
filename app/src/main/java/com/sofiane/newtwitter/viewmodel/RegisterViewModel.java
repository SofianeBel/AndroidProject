package com.sofiane.newtwitter.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.sofiane.newtwitter.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewModel responsable de la gestion de l'inscription des utilisateurs.
 * Cette classe gère le processus d'inscription, y compris la création de compte Firebase Auth
 * et l'enregistrement des données utilisateur dans Firestore.
 * Elle fournit des données observables sur l'état de l'inscription et les messages d'erreur.
 */
public class RegisterViewModel extends ViewModel {
    private static final String TAG = "RegisterViewModel";
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    // Variable pour suivre l'état de l'inscription
    private boolean registrationSuccessful = false;
    private User lastRegisteredUser = null;

    /**
     * Constructeur qui initialise Firebase Authentication et Firestore.
     */
    public RegisterViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Enregistre un nouvel utilisateur avec un nom d'utilisateur, email et mot de passe.
     * Crée un compte Firebase Auth, met à jour le profil utilisateur et enregistre les données dans Firestore.
     * Met à jour les LiveData et variables d'état en fonction du résultat.
     *
     * @param username Le nom d'utilisateur
     * @param email L'adresse email
     * @param password Le mot de passe
     */
    public void register(String username, String email, String password) {
        Log.d(TAG, "Starting registration process for email: " + email);
        
        // Réinitialiser les variables d'état
        registrationSuccessful = false;
        lastRegisteredUser = null;
        
        // Réinitialiser les LiveData avant de commencer
        errorMessage.setValue(null);
        
        // Tentative directe de création du compte sans vérification préalable
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
                            // Create user document in Firestore
                            User user = new User(firebaseUser.getUid(), username, email);
                            db.collection("users")
                                .document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "User document created in Firestore");
                                    
                                    // Déconnecter l'utilisateur pour qu'il doive se connecter explicitement
                                    auth.signOut();
                                    Log.d(TAG, "User signed out after registration");
                                    
                                    // Marquer l'inscription comme réussie
                                    registrationSuccessful = true;
                                    lastRegisteredUser = user;
                                    
                                    // Mettre à jour le LiveData avec les informations utilisateur
                                    // Utiliser setValue au lieu de postValue pour une mise à jour immédiate
                                    currentUser.setValue(user);
                                    Log.d(TAG, "currentUser LiveData updated with new user: " + user.getEmail());
                                    
                                    // Vérifier que le LiveData a bien été mis à jour
                                    if (currentUser.getValue() == null) {
                                        Log.e(TAG, "ERROR: currentUser LiveData is still null after setValue!");
                                    } else {
                                        Log.d(TAG, "CONFIRMED: currentUser LiveData contains: " + currentUser.getValue().getEmail());
                                    }
                                    
                                    // Réinitialiser le message d'erreur
                                    errorMessage.setValue(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create user profile in Firestore: " + e.getMessage());
                                    // Déconnecter l'utilisateur en cas d'erreur
                                    auth.signOut();
                                    errorMessage.setValue("Failed to create user profile: " + e.getMessage());
                                });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update profile: " + e.getMessage());
                            // Déconnecter l'utilisateur en cas d'erreur
                            auth.signOut();
                            errorMessage.setValue("Failed to update profile: " + e.getMessage());
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Registration failed: " + e.getMessage());
                
                // Si l'erreur est que l'email existe déjà, essayer de supprimer l'utilisateur de Firestore
                if (e.getMessage() != null && e.getMessage().contains("email address is already in use")) {
                    // Tenter de supprimer le document Firestore correspondant
                    db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Document deleted successfully, please try registration again");
                                        errorMessage.setValue("This email was previously used. Please try registration again.");
                                    })
                                    .addOnFailureListener(ex -> {
                                        Log.e(TAG, "Error deleting document: " + ex.getMessage());
                                        errorMessage.setValue("Error cleaning up old account: " + ex.getMessage());
                                    });
                            } else {
                                errorMessage.setValue("Registration failed: " + e.getMessage() + ". Please try with a different email.");
                            }
                        })
                        .addOnFailureListener(ex -> {
                            Log.e(TAG, "Error checking if email exists: " + ex.getMessage());
                            errorMessage.setValue("Registration failed: " + e.getMessage());
                        });
                } else {
                    errorMessage.setValue("Registration failed: " + e.getMessage());
                }
            });
    }

    /**
     * Obtient le LiveData contenant l'utilisateur nouvellement inscrit.
     *
     * @return LiveData contenant l'objet User de l'utilisateur inscrit, ou null si l'inscription a échoué
     */
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    /**
     * Obtient le LiveData contenant les messages d'erreur.
     *
     * @return LiveData contenant les messages d'erreur
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Vérifie si l'inscription a réussi.
     *
     * @return true si l'inscription a réussi, false sinon
     */
    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
    
    /**
     * Obtient l'utilisateur qui vient d'être inscrit.
     *
     * @return L'objet User de l'utilisateur inscrit, ou null si l'inscription a échoué
     */
    public User getLastRegisteredUser() {
        return lastRegisteredUser;
    }
} 