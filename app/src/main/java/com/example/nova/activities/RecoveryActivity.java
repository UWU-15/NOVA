// RecoveryActivity.java
package com.example.nova.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RecoveryActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private Button sendLinkButton;
    private TextView backToSignInButton;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        firebaseService = FirebaseService.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.emailLayout);
        emailInput = findViewById(R.id.emailInput);
        sendLinkButton = findViewById(R.id.sendLinkButton);
        backToSignInButton = findViewById(R.id.backToSignInButton);
    }

    private void setupClickListeners() {
        sendLinkButton.setOnClickListener(v -> sendResetLink());
        backToSignInButton.setOnClickListener(v -> finish());
    }

    private void sendResetLink() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        firebaseService.resetPassword(email, new FirebaseService.OnResetListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(RecoveryActivity.this,
                        "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RecoveryActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}