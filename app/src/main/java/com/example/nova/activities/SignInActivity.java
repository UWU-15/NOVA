// SignInActivity.java
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

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private Button actionButton;
    private TextView registerNowText, forgotPasswordText;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseService = FirebaseService.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        actionButton = findViewById(R.id.actionButton);
        registerNowText = findViewById(R.id.registerNowText);
        forgotPasswordText = findViewById(R.id.recoveryPasswordText);
    }

    private void setupClickListeners() {
        actionButton.setOnClickListener(v -> attemptLogin());

        registerNowText.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
        });

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, RecoveryActivity.class));
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        } else {
            passwordLayout.setError(null);
        }

        firebaseService.signIn(email, password, new FirebaseService.OnAuthListener() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                // After successful login → check genres in LoadingActivity
                Intent intent = new Intent(SignInActivity.this, LoadingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(SignInActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}