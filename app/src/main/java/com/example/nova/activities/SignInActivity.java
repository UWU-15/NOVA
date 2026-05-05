package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
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

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private Button actionButton;
    private TextView registerNowText, recoveryPasswordText;

    private FirebaseService firebaseService;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseService = FirebaseService.getInstance();

        initViews();
        setupClickListeners();
        setupTextWatchers();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        actionButton = findViewById(R.id.actionButton);
        registerNowText = findViewById(R.id.registerNowText);
        recoveryPasswordText = findViewById(R.id.recoveryPasswordText);
    }

    private void setupClickListeners() {

        actionButton.setOnClickListener(v -> {
            if (!isLoading) attemptLogin();
        });

        registerNowText.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        recoveryPasswordText.setOnClickListener(v ->
                startActivity(new Intent(this, RecoveryActivity.class)));
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailLayout.setError(null);
                passwordLayout.setError(null);
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        emailInput.addTextChangedListener(watcher);
        passwordInput.addTextChangedListener(watcher);
    }

    private void attemptLogin() {
        hideKeyboard();

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateFields(email, password)) return;

        setLoading(true);

        firebaseService.signIn(email, password, new FirebaseService.OnAuthListener() {

            @Override
            public void onSuccess(FirebaseUser user) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);

                String message = getLoginErrorMessage(error);
                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        actionButton.setEnabled(!loading);

        if (loading) {
            actionButton.setText("Signing in...");
            actionButton.setAlpha(0.7f);
        } else {
            actionButton.setText(getString(R.string.sign_in));
            actionButton.setAlpha(1f);
        }
    }

    private boolean validateFields(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return false;
        }

        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private String getLoginErrorMessage(String error) {
        String e = error.toLowerCase();

        if (e.contains("password") || e.contains("user-not-found") || e.contains("invalid")) {
            return "Invalid email or password";
        }

        if (e.contains("network")) {
            return "Network error";
        }

        if (e.contains("too many")) {
            return "Too many attempts";
        }

        return "Login failed";
    }

    // UX: тап вне полей закрывает клавиатуру
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}