package com.sofiane.newtwitter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.sofiane.newtwitter.databinding.ActivityLoginBinding;
import com.sofiane.newtwitter.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Récupérer l'email s'il a été passé depuis RegisterActivity
        if (getIntent().hasExtra("EMAIL")) {
            String email = getIntent().getStringExtra("EMAIL");
            binding.emailEditText.setText(email);
            // Mettre le focus sur le champ de mot de passe
            binding.passwordEditText.requestFocus();
        }

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> {
            // Show loading indicator
            binding.progressBar.setVisibility(View.VISIBLE);
            
            String email = binding.emailEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            Log.d(TAG, "Attempting to login with email: " + email);
            viewModel.login(email, password);
            
            // Ajouter un timeout pour éviter le chargement infini
            binding.getRoot().postDelayed(() -> {
                if (binding.progressBar.getVisibility() == View.VISIBLE) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login timed out. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Login timed out");
                }
            }, 30000); // 30 secondes de timeout
        });

        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getCurrentUser().observe(this, user -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (user != null) {
                Log.d(TAG, "Login successful for user: " + user.getEmail());
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                
                try {
                    // User logged in successfully, navigate to MainActivity
                    Log.d(TAG, "Navigating to MainActivity...");
                    
                    // Ajouter un délai pour s'assurer que le toast est affiché
                    binding.getRoot().postDelayed(() -> {
                        try {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Log.d(TAG, "Starting MainActivity intent");
                            startActivity(intent);
                            Log.d(TAG, "MainActivity intent started, finishing LoginActivity");
                            finish(); // Close LoginActivity
                        } catch (Exception e) {
                            Log.e(TAG, "Error navigating to MainActivity: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Error navigating to main screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }, 500); // Délai de 500ms
                } catch (Exception e) {
                    Log.e(TAG, "Error preparing navigation to MainActivity: " + e.getMessage(), e);
                    Toast.makeText(this, "Error navigating to main screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (errorMessage != null) {
                Log.e(TAG, "Login error: " + errorMessage);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
} 