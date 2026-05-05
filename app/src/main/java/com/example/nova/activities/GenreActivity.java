package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GenreActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private MaterialButton okButton;

    private FirebaseService firebaseService;
    private FirebaseFirestore firestore;

    private final List<String> selectedGenres = new ArrayList<>();
    private static final int MIN_GENRES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        firebaseService = FirebaseService.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initViews();
        loadGenresFromBackend();
        setupClick();
        updateUI();
    }

    private void initViews() {
        chipGroup = findViewById(R.id.chipGroup);
        okButton = findViewById(R.id.okButton);
    }

    // 🔥 ГЛАВНОЕ ИЗМЕНЕНИЕ — динамика
    private void loadGenresFromBackend() {
        firestore.collection("genres")
                .get()
                .addOnSuccessListener(snapshot -> {

                    chipGroup.removeAllViews();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String genreName = doc.getString("name");
                        if (genreName == null) continue;

                        Chip chip = new Chip(this);
                        chip.setText(genreName);
                        chip.setCheckable(true);
                        chip.setClickable(true);
                        chip.setChipBackgroundColorResource(R.color.chip_state_color);

                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {

                            if (isChecked) {
                                if (!selectedGenres.contains(genreName)) {
                                    selectedGenres.add(genreName);
                                }
                            } else {
                                selectedGenres.remove(genreName);
                            }

                            updateUI();
                        });

                        chipGroup.addView(chip);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load genres", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateUI() {
        int count = selectedGenres.size();

        okButton.setEnabled(count >= MIN_GENRES);

        if (count >= MIN_GENRES) {
            okButton.setAlpha(1f);
            okButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.light_beige)
            );
            okButton.setTextColor(ContextCompat.getColor(this, R.color.dark_blue));
        } else {
            okButton.setAlpha(0.35f);
            okButton.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.dark_blue)
            );
            okButton.setTextColor(0x88FFFFFF);
        }

        ((android.widget.TextView) findViewById(R.id.tagCounter))
                .setText(count + "/" + MIN_GENRES + "+");
    }

    private void setupClick() {
        okButton.setOnClickListener(v -> saveGenres());
    }

    private void saveGenres() {

        FirebaseUser user = firebaseService.getCurrentUser();
        if (user == null) return;

        okButton.setEnabled(false);
        okButton.setText("Saving...");

        firestore.collection("users")
                .document(user.getUid())
                .update(
                        "favoriteGenres", selectedGenres,
                        "isSetupComplete", true
                )
                .addOnSuccessListener(unused -> {
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                })
                .addOnFailureListener(e -> {
                    okButton.setEnabled(true);
                    okButton.setText("OK");
                    Toast.makeText(this, "Save error", Toast.LENGTH_SHORT).show();
                });
    }
}