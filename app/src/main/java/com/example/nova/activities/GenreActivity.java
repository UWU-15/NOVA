// GenreActivity.java
package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.example.nova.models.User;
import com.example.nova.services.FirebaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class GenreActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private TextView tagCounter;
    private MaterialButton okButton;
    private List<String> selectedGenres = new ArrayList<>();
    private static final int MAX_SELECTION = 3;
    private FirebaseService firebaseService;

    // Available genres
    private final String[] availableGenres = {
            "Rock", "Pop", "Hip Hop", "Electronic", "Jazz", "Classical",
            "R&B", "Country", "Metal", "Indie", "Folk", "Blues",
            "Reggae", "Punk", "Soul", "Funk", "Disco", "Latin"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        firebaseService = FirebaseService.getInstance();
        initViews();
        setupChips();
        setupClickListeners();
    }

    private void initViews() {
        chipGroup = findViewById(R.id.chipGroup);
        tagCounter = findViewById(R.id.tagCounter);
        okButton = findViewById(R.id.okButton);
        updateCounter();
    }

    private void setupChips() {
        // Dynamically create chips if not in XML
        for (String genre : availableGenres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setClickable(true);
            chipGroup.addView(chip);
        }

        // Setup chip listeners
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            android.view.View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (selectedGenres.size() >= MAX_SELECTION) {
                            chip.setChecked(false);
                            Toast.makeText(this, "You can select up to " + MAX_SELECTION + " genres",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            selectedGenres.add(chip.getText().toString());
                            updateCounter();
                        }
                    } else {
                        selectedGenres.remove(chip.getText().toString());
                        updateCounter();
                    }
                });
            }
        }
    }

    private void setupClickListeners() {
        okButton.setOnClickListener(v -> saveGenresAndProceed());
    }

    private void updateCounter() {
        tagCounter.setText(selectedGenres.size() + "/" + MAX_SELECTION);
        okButton.setEnabled(selectedGenres.size() == MAX_SELECTION);
    }

    private void saveGenresAndProceed() {
        FirebaseUser currentUser = firebaseService.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
            return;
        }

        String userId = currentUser.getUid();

        firebaseService.updateUserGenres(userId, selectedGenres, new FirebaseService.OnUpdateListener() {
            @Override
            public void onSuccess() {
                // Genres saved successfully → go to LoadingActivity
                // LoadingActivity will check genres and redirect to MainActivity
                Toast.makeText(GenreActivity.this, "Genres saved!", Toast.LENGTH_SHORT).show();
                navigateToLoading();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(GenreActivity.this, "Error saving genres: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLoading() {
        Intent intent = new Intent(GenreActivity.this, LoadingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(GenreActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}