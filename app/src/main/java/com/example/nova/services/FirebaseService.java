// FirebaseService.java
package com.example.nova.services;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.example.nova.models.Playlist;
import com.example.nova.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class FirebaseService {

    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    // ==================== AUTHENTICATION ====================

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signUp(String email, String password, String name, OnAuthListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Create user document in Firestore
                            User newUser = new User(user.getUid(), name, email);
                            db.collection("users").document(user.getUid())
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> listener.onSuccess(user))
                                    .addOnFailureListener(e -> listener.onFailure("Failed to save user: " + e.getMessage()));
                        } else {
                            listener.onFailure("User is null");
                        }
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    public void signIn(String email, String password, OnAuthListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess(auth.getCurrentUser());
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void signOut() {
        auth.signOut();
    }

    public void resetPassword(String email, OnResetListener listener) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException().getMessage() : "Failed to send reset email");
                    }
                });
    }

    // ==================== USER DATA ====================

    public void getUserData(String userId, OnUserDataListener listener) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateUserData(User user, OnUpdateListener listener) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateUserGenres(String userId, List<String> genres, OnUpdateListener listener) {
        db.collection("users").document(userId)
                .update("favoriteGenres", genres)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ==================== LIKED SONGS ====================

    public void addLikedSong(String userId, String trackId, OnUpdateListener listener) {
        db.collection("users").document(userId)
                .update("likedSongs", com.google.firebase.firestore.FieldValue.arrayUnion(trackId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void removeLikedSong(String userId, String trackId, OnUpdateListener listener) {
        db.collection("users").document(userId)
                .update("likedSongs", com.google.firebase.firestore.FieldValue.arrayRemove(trackId))
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ==================== PLAYLISTS ====================

    public void getUserPlaylists(String userId, OnPlaylistsListener listener) {
        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Playlist> playlists = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Playlist playlist = doc.toObject(Playlist.class);
                        if (playlist != null) {
                            playlist.setId(doc.getId());
                            playlists.add(playlist);
                        }
                    }
                    listener.onSuccess(playlists);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void createPlaylist(String userId, String name, OnPlaylistListener listener) {
        Playlist playlist = new Playlist(name, userId);
        db.collection("playlists")
                .add(playlist)
                .addOnSuccessListener(documentReference -> {
                    playlist.setId(documentReference.getId());
                    listener.onSuccess(playlist);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ==================== PHOTO UPLOAD ====================

    public void uploadUserPhoto(Uri imageUri, String userId, OnPhotoUploadListener listener) {
        StorageReference ref = storage.getReference().child("profile_photos/" + userId + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        db.collection("users").document(userId)
                                .update("photoUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> listener.onSuccess(uri.toString()))
                                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                    });
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ==================== LISTENERS ====================

    public interface OnAuthListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public interface OnResetListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserDataListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnPlaylistsListener {
        void onSuccess(List<Playlist> playlists);
        void onFailure(String error);
    }

    public interface OnPlaylistListener {
        void onSuccess(Playlist playlist);
        void onFailure(String error);
    }

    public interface OnPhotoUploadListener {
        void onSuccess(String url);
        void onFailure(String error);
    }
}