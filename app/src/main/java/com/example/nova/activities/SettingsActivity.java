// SettingsActivity.java
package com.example.nova.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvSettings, btnLogout;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private Button btnChangeTags;
    private FirebaseService firebaseService;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null && firebaseService.getCurrentUser() != null) {
                        firebaseService.uploadUserPhoto(imageUri, firebaseService.getCurrentUser().getUid(),
                                new FirebaseService.OnPhotoUploadListener() {
                                    @Override
                                    public void onSuccess(String url) {
                                        Toast.makeText(SettingsActivity.this, "Photo updated", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(SettingsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseService = FirebaseService.getInstance();
        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvSettings = findViewById(R.id.tvSettings);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnChangeTags = findViewById(R.id.btnChangeTags);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangeTags.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, GenreActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> performLogout());

        // Profile image click (if you add an ImageView for profile)
        // profileImage.setOnClickListener(v -> openImagePicker());
    }

    private void loadUserData() {
        FirebaseUser user = firebaseService.getCurrentUser();
        if (user != null) {
            emailInput.setText(user.getEmail());

            firebaseService.getUserData(user.getUid(), new FirebaseService.OnUserDataListener() {
                @Override
                public void onSuccess(com.example.nova.models.User userData) {
                    // Load user data if needed
                }

                @Override
                public void onFailure(String error) {
                    // Handle error
                }
            });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void performLogout() {
        firebaseService.signOut();
        Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}