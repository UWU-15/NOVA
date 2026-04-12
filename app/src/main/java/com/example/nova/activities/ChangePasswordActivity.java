// ChangePasswordActivity.java
package com.example.nova.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout newPasswordLayout, confirmPasswordLayout;
    private TextInputEditText newPasswordInput, confirmPasswordInput;
    private Button changePasswordButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        changePasswordButton = findViewById(R.id.changePasswordButton);
    }

    private void setupClickListeners() {
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        boolean isValid = true;

        if (newPassword.isEmpty()) {
            newPasswordLayout.setError("Password is required");
            isValid = false;
        } else if (newPassword.length() < 6) {
            newPasswordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            newPasswordLayout.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Please confirm password");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (isValid) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this,
                                        "Password changed successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ChangePasswordActivity.this,
                                        "Failed to change password", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}