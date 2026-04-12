// RegisterActivity.java
package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button createAccountButton;
    private TextView backToSignInText;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseService = FirebaseService.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        createAccountButton = findViewById(R.id.createAccountButton);
        backToSignInText = findViewById(R.id.signInText);
    }

    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> attemptRegistration());
        backToSignInText.setOnClickListener(v -> finish());
    }

    private void attemptRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (isValid) {
            performRegistration(name, email, password);
        }
    }

    private void performRegistration(String name, String email, String password) {
        firebaseService.signUp(email, password, name, new FirebaseService.OnAuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Registration successful → go to GenreActivity to select genres
                Intent intent = new Intent(RegisterActivity.this, GenreActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}