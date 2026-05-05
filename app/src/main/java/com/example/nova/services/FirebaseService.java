package com.example.nova.services;

import android.net.Uri;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Map;

public class FirebaseService {

    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) instance = new FirebaseService();
        return instance;
    }

    // --- ИНТЕРФЕЙСЫ (ВОССТАНОВЛЕНЫ ВСЕ) ---
    public interface OnAuthListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserDataListener {
        void onSuccess(Map<String, Object> userData);
        void onFailure(Exception e);
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Эти интерфейсы нужны для LibraryActivity и RecoveryActivity
    public interface OnPlaylistListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnResetListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnPhotoUploadListener {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    public interface OnPlaylistsListener {
        void onSuccess(List<Playlist> playlists);
        void onFailure(Exception e);
    }

    // --- МЕТОДЫ AUTH ---
    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }

    public void signOut() { auth.signOut(); }

    public void signIn(String email, String password, OnAuthListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(auth.getCurrentUser());
                    else listener.onFailure(getErrorMessage(task.getException()));
                });
    }

    public void register(String email, String password, OnAuthListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(auth.getCurrentUser());
                    else listener.onFailure(getErrorMessage(task.getException()));
                });
    }

    // Метод для RecoveryActivity
    public void resetPassword(String email, OnResetListener listener) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess();
                    else listener.onFailure(getErrorMessage(task.getException()));
                });
    }

    // Метод для ChangePasswordActivity (App Links)
    public void sendCustomPasswordReset(String email, SimpleCallback callback) {
        String projectDomain = "https://nova-cccd7.firebaseapp.com/__/auth/action";
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(projectDomain)
                .setHandleCodeInApp(true)
                .setAndroidPackageName("com.example.nova", true, null)
                .build();

        auth.sendPasswordResetEmail(email, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onFailure(getErrorMessage(task.getException()));
                });
    }

    public void confirmPasswordReset(String code, String newPassword, SimpleCallback callback) {
        auth.confirmPasswordReset(code, newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess();
                    else callback.onFailure(getErrorMessage(task.getException()));
                });
    }

    public void updatePassword(String newPassword, SimpleCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) callback.onSuccess();
                        else callback.onFailure(getErrorMessage(task.getException()));
                    });
        }
    }

    // --- МЕТОДЫ FIRESTORE ---
    public void getUserData(String userId, OnUserDataListener listener) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> listener.onSuccess(doc.getData()))
                .addOnFailureListener(listener::onFailure);
    }

    public void updateUserData(Map<String, Object> data, OnUpdateListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        String uid = (user != null) ? user.getUid() : "unknown";
        db.collection("users").document(uid).update(data)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    // --- ЗАГЛУШКИ (ИСПРАВЛЕНЫ) ---
    public void addLikedSong(String userId, String trackId, OnUpdateListener listener) {}
    public void removeLikedSong(String userId, String trackId, OnUpdateListener listener) {}
    public void getUserPlaylists(String userId, OnPlaylistsListener listener) {}
    public void createPlaylist(String userId, String name, OnPlaylistListener listener) {}
    public void uploadUserPhoto(Uri uri, String uid, OnPhotoUploadListener listener) {}

    private String getErrorMessage(Exception e) {
        return (e != null) ? e.getLocalizedMessage() : "Unknown error";
    }
}