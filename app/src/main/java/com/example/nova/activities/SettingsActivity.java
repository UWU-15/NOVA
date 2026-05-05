package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView btnLogout;
    private EditText etDisplayName;
    private TextInputEditText emailInput, passwordInput;
    private Button btnChangeTags, btnSaveSettings;

    private FirebaseService firebaseService;
    private FirebaseFirestore firestore;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseService = FirebaseService.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = firebaseService.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        etDisplayName = findViewById(R.id.etDisplayName);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnChangeTags = findViewById(R.id.btnChangeTags);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
    }

    private void loadUserData() {
        // Загружаем email из Auth
        emailInput.setText(firebaseService.getCurrentUser().getEmail());

        // Загружаем имя из Firestore
        firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) etDisplayName.setText(name);
                    }
                });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // АВТОМАТИЧЕСКОЕ СОХРАНЕНИЕ ИМЕНИ ПРИ ИЗМЕНЕНИИ
        etDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newName = s.toString().trim();
                if (!newName.isEmpty()) {
                    firestore.collection("users").document(currentUserId)
                            .update("name", newName);
                }
            }
        });

        // СОХРАНЕНИЕ EMAIL И ПАРОЛЯ
        btnSaveSettings.setOnClickListener(v -> {
            String newEmail = emailInput.getText().toString().trim();
            String newPass = passwordInput.getText().toString().trim();
            FirebaseUser user = firebaseService.getCurrentUser();

            if (!newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
                user.updateEmail(newEmail).addOnSuccessListener(aVoid -> {
                    firestore.collection("users").document(currentUserId).update("email", newEmail);
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(this, "Error updating email", Toast.LENGTH_SHORT).show());
            }

            if (!newPass.isEmpty()) {
                user.updatePassword(newPass).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                    passwordInput.setText(""); // Очищаем поле
                });
            }
        });

        btnChangeTags.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, GenreActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            firebaseService.signOut();
            Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}