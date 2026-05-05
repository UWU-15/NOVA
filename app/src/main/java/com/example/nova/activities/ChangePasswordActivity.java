package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout newPasswordLayout, confirmPasswordLayout;
    private TextInputEditText newPasswordInput, confirmPasswordInput;
    private Button changePasswordButton;

    private FirebaseService firebaseService;
    private String oobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseService = FirebaseService.getInstance();

        handleIntent(getIntent());
        initViews();
        setupListeners();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            oobCode = intent.getData().getQueryParameter("oobCode");
        }
    }

    private void initViews() {
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        changePasswordButton = findViewById(R.id.changePasswordButton);
    }

    private void setupListeners() {

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newPasswordLayout.setError(null);
                confirmPasswordLayout.setError(null);
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        newPasswordInput.addTextChangedListener(watcher);
        confirmPasswordInput.addTextChangedListener(watcher);

        changePasswordButton.setOnClickListener(v -> attemptChangePassword());
    }

    private void attemptChangePassword() {
        hideKeyboard();

        String pass1 = newPasswordInput.getText() != null
                ? newPasswordInput.getText().toString().trim()
                : "";

        String pass2 = confirmPasswordInput.getText() != null
                ? confirmPasswordInput.getText().toString().trim()
                : "";

        if (pass1.length() < 6) {
            newPasswordLayout.setError("Min 6 characters");
            return;
        }

        if (!pass1.equals(pass2)) {
            confirmPasswordLayout.setError("Passwords don't match");
            return;
        }

        setLoading(true);

        if (oobCode != null) {
            firebaseService.confirmPasswordReset(oobCode, pass1, new FirebaseService.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Password changed", Toast.LENGTH_SHORT).show();

                    goToSignIn();
                }

                @Override
                public void onFailure(String error) {
                    setLoading(false);
                    Toast.makeText(ChangePasswordActivity.this,
                            "Link expired", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            firebaseService.updatePassword(pass1, new FirebaseService.SimpleCallback() {
                @Override
                public void onSuccess() {
                    firebaseService.signOut();
                    goToSignIn();
                }

                @Override
                public void onFailure(String error) {
                    setLoading(false);
                    Toast.makeText(ChangePasswordActivity.this,
                            error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void goToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        changePasswordButton.setEnabled(!loading);
        changePasswordButton.setText(loading ? "Processing..." : "Change Password");
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}