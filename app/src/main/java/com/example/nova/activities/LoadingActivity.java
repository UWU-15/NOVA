// LoadingActivity.java
package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nova.R;
import com.example.nova.models.User;
import com.example.nova.services.FirebaseService;
import com.google.firebase.auth.FirebaseUser;

public class LoadingActivity extends AppCompatActivity {

    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        firebaseService = FirebaseService.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = firebaseService.getCurrentUser();

            if (currentUser != null) {
                // User is logged in → check if genres are selected
                checkUserGenres(currentUser.getUid());
            } else {
                // No user → go to sign in
                navigateToSignIn();
            }
        }, 2000);
    }

    private void checkUserGenres(String userId) {
        firebaseService.getUserData(userId, new FirebaseService.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getFavoriteGenres() != null && !user.getFavoriteGenres().isEmpty()) {
                    // Genres are selected → go to MainActivity
                    navigateToMain();
                } else {
                    // No genres selected → go to GenreActivity
                    navigateToGenre();
                }
            }

            @Override
            public void onFailure(String error) {
                // Error loading user data → fallback to MainActivity
                Toast.makeText(LoadingActivity.this, "Error loading user data: " + error, Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(LoadingActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToGenre() {
        Intent intent = new Intent(LoadingActivity.this, GenreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}