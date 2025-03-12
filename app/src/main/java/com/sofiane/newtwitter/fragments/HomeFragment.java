package com.sofiane.newtwitter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Log.d("HomeFragment", "Creating HomeFragment view");
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error creating HomeFragment view: " + e.getMessage(), e);
            // Créer une vue simple en cas d'erreur
            TextView errorView = new TextView(getContext());
            errorView.setText("Error loading home feed. Please try again later.");
            errorView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            errorView.setGravity(Gravity.CENTER);
            errorView.setPadding(50, 50, 50, 50);
            return errorView;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize your home screen UI and functionality here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 