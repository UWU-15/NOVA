package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private TextInputEditText nameInput, emailInput, passwordInput;
    private Button createAccountButton;
    private TextView registerNowText;

    private FirebaseService firebaseService;
    private FirebaseFirestore firestore;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseService = FirebaseService.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        setupTextWatchers();
    }

    private void initViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        createAccountButton = findViewById(R.id.createAccountButton);
        registerNowText = findViewById(R.id.registerNowText);
    }

    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> {
            if (!isLoading) attemptRegister();
        });

        registerNowText.setOnClickListener(v -> finish());
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameLayout.setError(null);
                emailLayout.setError(null);
                passwordLayout.setError(null);
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        nameInput.addTextChangedListener(watcher);
        emailInput.addTextChangedListener(watcher);
        passwordInput.addTextChangedListener(watcher);
    }

    private void attemptRegister() {
        hideKeyboard();

        String name = getText(nameInput);
        String email = getText(emailInput);
        String password = getText(passwordInput);

        if (!validateFields(name, email, password)) return;

        setLoading(true);

        firebaseService.register(email, password, new FirebaseService.OnAuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                saveUser(user.getUid(), name, email);
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        formatError(error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUser(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("favoriteGenres", new ArrayList<>());
        user.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> goNext())
                .addOnFailureListener(e -> goNext());
    }

    private void goNext() {
        setLoading(false);
        hideKeyboard();

        Intent intent = new Intent(this, GenreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateFields(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Enter name");
            return false;
        }

        if (TextUtils.isEmpty(email) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email");
            return false;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Min 6 characters");
            return false;
        }

        return true;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        createAccountButton.setEnabled(!loading);
        createAccountButton.setText(loading ? "Creating..." : "Create Account");
    }

    private String getText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private String formatError(String error) {
        if (error == null) return "Unknown error";

        String e = error.toLowerCase();

        if (e.contains("already")) return "Email already exists";
        if (e.contains("network")) return "No internet connection";

        return "Error: " + error;
    }
}