package com.example.nova.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
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

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        firebaseService = FirebaseService.getInstance();

        initViews();
        setupClickListeners();
        setupTextWatcher();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.emailLayout);
        emailInput = findViewById(R.id.emailInput);
        sendLinkButton = findViewById(R.id.sendLinkButton);
        backToSignInButton = findViewById(R.id.backToSignInText);
    }

    private void setupClickListeners() {

        sendLinkButton.setOnClickListener(v -> {
            if (!isLoading) sendResetLink();
        });

        backToSignInButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupTextWatcher() {
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sendResetLink() {
        hideKeyboard();

        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            return;
        }

        setLoading(true);

        firebaseService.resetPassword(email, new FirebaseService.OnResetListener() {

            @Override
            public void onSuccess() {
                setLoading(false);

                Toast.makeText(
                        RecoveryActivity.this,
                        "Check your email for reset instructions",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);

                Toast.makeText(
                        RecoveryActivity.this,
                        getRecoveryErrorMessage(error),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        sendLinkButton.setEnabled(!loading);
        sendLinkButton.setAlpha(loading ? 0.6f : 1f);
        sendLinkButton.setText(loading ? "Sending..." : "Send Link");
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private String getRecoveryErrorMessage(String error) {
        String lowerError = error.toLowerCase();

        if (lowerError.contains("user-not-found")) {
            return "No account found with this email";
        }

        if (lowerError.contains("network")) {
            return "Check your internet connection";
        }

        if (lowerError.contains("too many")) {
            return "Too many attempts. Try later";
        }

        return "Failed to send reset link";
    }
}