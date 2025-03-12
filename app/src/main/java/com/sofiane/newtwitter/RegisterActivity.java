package com.sofiane.newtwitter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.sofiane.newtwitter.databinding.ActivityRegisterBinding;
import com.sofiane.newtwitter.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.registerButton.setOnClickListener(v -> {
            // Show loading indicator
            binding.progressBar.setVisibility(View.VISIBLE);
            
            String username = binding.usernameEditText.getText().toString();
            String email = binding.emailEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();
            String confirmPassword = binding.confirmPasswordEditText.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            Log.d(TAG, "Attempting to register user: " + email);
            viewModel.register(username, email, password);
            
            // Vérifier immédiatement si l'inscription a réussi (pour les cas où la réponse est très rapide)
            handler.postDelayed(() -> {
                if (binding.progressBar.getVisibility() == View.VISIBLE && 
                    (viewModel.isRegistrationSuccessful() || viewModel.getCurrentUser().getValue() != null)) {
                    
                    Log.d(TAG, "Registration completed quickly, updating UI");
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // Utiliser soit le lastRegisteredUser, soit la valeur du LiveData
                    String userEmail = viewModel.getLastRegisteredUser() != null 
                        ? viewModel.getLastRegisteredUser().getEmail() 
                        : viewModel.getCurrentUser().getValue().getEmail();
                        
                    showSuccessDialog(userEmail);
                }
            }, 2000); // Vérifier après 2 secondes
            
            // Ajouter un timeout pour éviter le chargement infini
            handler.postDelayed(() -> {
                if (binding.progressBar.getVisibility() == View.VISIBLE) {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // Vérifier si l'inscription a réussi malgré le timeout de l'UI
                    if (viewModel.isRegistrationSuccessful() || viewModel.getCurrentUser().getValue() != null) {
                        Log.d(TAG, "Registration was actually successful despite UI timeout");
                        
                        // Utiliser soit le lastRegisteredUser, soit la valeur du LiveData
                        String userEmail = viewModel.getLastRegisteredUser() != null 
                            ? viewModel.getLastRegisteredUser().getEmail() 
                            : viewModel.getCurrentUser().getValue().getEmail();
                            
                        showSuccessDialog(userEmail);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration timed out. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration timed out");
                    }
                }
            }, 30000); // 30 secondes de timeout
        });

        binding.loginButton.setOnClickListener(v -> {
            // Rediriger vers LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Méthode pour afficher le dialogue de succès
    private void showSuccessDialog(String email) {
        // Annuler le timeout si l'inscription réussit
        handler.removeCallbacksAndMessages(null);
        
        // Afficher une alerte de confirmation plus visible
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Inscription réussie");
        builder.setMessage("Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
        builder.setPositiveButton("Se connecter", (dialog, which) -> {
            // Rediriger vers LoginActivity avec l'email pré-rempli
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void observeViewModel() {
        Log.d(TAG, "Setting up observers for LiveData");
        
        viewModel.getCurrentUser().observe(this, user -> {
            Log.d(TAG, "CurrentUser LiveData changed: " + (user != null ? "User received" : "User is null"));
            binding.progressBar.setVisibility(View.GONE);
            
            if (user != null) {
                Log.d(TAG, "Registration successful for user: " + user.getEmail());
                
                // Afficher le dialogue de succès
                showSuccessDialog(user.getEmail());
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            Log.d(TAG, "ErrorMessage LiveData changed: " + (errorMessage != null ? errorMessage : "Error is null"));
            // Assurez-vous que le progressBar est masqué
            binding.progressBar.setVisibility(View.GONE);
            
            // Annuler le timeout en cas d'erreur
            handler.removeCallbacksAndMessages(null);
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, "Registration error: " + errorMessage);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
} 