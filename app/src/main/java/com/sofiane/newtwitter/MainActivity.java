package com.sofiane.newtwitter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sofiane.newtwitter.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "Starting MainActivity onCreate");
            
            // Initialiser Firebase Auth
            auth = FirebaseAuth.getInstance();
            
            // Vérifier si l'utilisateur est connecté
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                // L'utilisateur n'est pas connecté, rediriger vers LoginActivity
                Log.d(TAG, "User not logged in, redirecting to LoginActivity");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
                return;
            }
            
            // L'utilisateur est connecté, continuer avec l'initialisation normale
            Log.d(TAG, "User logged in: " + currentUser.getEmail() + ", UID: " + currentUser.getUid());
            
            try {
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                Log.d(TAG, "Layout inflated successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error inflating layout: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            try {
                // Set up the toolbar
                if (binding.toolbar != null) {
                    setSupportActionBar(binding.toolbar);
                    Log.d(TAG, "Toolbar set up successfully");
                } else {
                    Log.e(TAG, "Toolbar is null");
                }

                // Set up the bottom navigation with the nav controller
                BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
                if (bottomNavigationView == null) {
                    Log.e(TAG, "BottomNavigationView is null");
                    Toast.makeText(this, "Error: Navigation view not found", Toast.LENGTH_LONG).show();
                    return;
                }
                
                try {
                    // Utiliser NavHostFragment pour obtenir le NavController
                    Log.d(TAG, "Finding NavHostFragment with ID: " + R.id.nav_host_fragment);
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment);
                    
                    if (navHostFragment == null) {
                        Log.e(TAG, "NavHostFragment is null - fragment not found in layout");
                        Toast.makeText(this, "Error: Navigation host fragment not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    Log.d(TAG, "NavHostFragment found: " + navHostFragment);
                    navController = navHostFragment.getNavController();
                    if (navController == null) {
                        Log.e(TAG, "NavController is null - could not get controller from fragment");
                        Toast.makeText(this, "Error: NavController not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    Log.d(TAG, "NavController initialized successfully: " + navController);
                    Log.d(TAG, "Current destination: " + (navController.getCurrentDestination() != null ? 
                            navController.getCurrentDestination().getLabel() : "null"));
                } catch (Exception e) {
                    Log.e(TAG, "Error finding NavController: " + e.getMessage(), e);
                    Toast.makeText(this, "Error initializing navigation: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                // Configure the top level destinations
                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.navigation_home,
                        R.id.navigation_profile,
                        R.id.navigation_settings
                ).build();

                // Set up the ActionBar with NavController
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                
                // Set up the bottom navigation with NavController
                NavigationUI.setupWithNavController(bottomNavigationView, navController);
                Log.d(TAG, "Navigation setup completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
                Toast.makeText(this, "Error setting up navigation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in MainActivity.onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Fatal error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Rediriger vers LoginActivity en cas d'erreur fatale
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // Double-check authentication state when activity starts/resumes
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.d(TAG, "User not logged in (onStart check), redirecting to LoginActivity");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart: " + e.getMessage(), e);
            // Rediriger vers LoginActivity en cas d'erreur
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            return navController.navigateUp() || super.onSupportNavigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Error in onSupportNavigateUp: " + e.getMessage(), e);
            return super.onSupportNavigateUp();
        }
    }
} 