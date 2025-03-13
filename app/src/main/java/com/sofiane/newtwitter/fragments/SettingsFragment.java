package com.sofiane.newtwitter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.databinding.FragmentSettingsBinding;

/**
 * Fragment permettant à l'utilisateur d'accéder aux paramètres de l'application.
 * Ce fragment offre des options pour éditer le profil, changer le mot de passe,
 * et se déconnecter de l'application. Il vérifie également l'état d'authentification
 * de l'utilisateur avant de permettre certaines actions.
 */
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private FirebaseAuth auth;

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
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initialise les composants de l'interface utilisateur et configure les listeners
     * après que la vue a été créée.
     *
     * @param view La vue racine du fragment
     * @param savedInstanceState L'état sauvegardé du fragment
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        
        // Initialize settings UI and load user preferences
        setupSettingsOptions();
    }

    private void setupSettingsOptions() {
        // Set up edit profile button
        binding.editProfileButton.setOnClickListener(v -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_editProfileFragment);
            } else {
                Toast.makeText(requireContext(), "Vous devez être connecté pour modifier votre profil", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Set up change password button
        binding.changePasswordButton.setOnClickListener(v -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // TODO: Implement change password functionality
                Toast.makeText(requireContext(), "Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Vous devez être connecté pour changer votre mot de passe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 