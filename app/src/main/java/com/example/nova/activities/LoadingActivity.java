package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nova.R;
import com.example.nova.services.FirebaseService;
import com.google.firebase.auth.FirebaseUser;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        checkAuth();
        startAnimations();
    }

    private void checkAuth() {

        FirebaseUser currentUser = FirebaseService.getInstance().getCurrentUser();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Intent intent;

            if (currentUser != null) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, SignInActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

        }, 700); // мягкий UX delay
    }

    private void startAnimations() {
        // пока оставляем заглушку )
    }
}