// User.java
package com.example.nova.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private List<String> favoriteGenres;
    private List<String> likedSongs;
    private List<String> playlistIds;
    private long createdAt;

    public User() {
        // Empty constructor for Firestore
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.favoriteGenres = new ArrayList<>();
        this.likedSongs = new ArrayList<>();
        this.playlistIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public List<String> getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(List<String> favoriteGenres) { this.favoriteGenres = favoriteGenres; }

    public List<String> getLikedSongs() { return likedSongs; }
    public void setLikedSongs(List<String> likedSongs) { this.likedSongs = likedSongs; }

    public List<String> getPlaylistIds() { return playlistIds; }
    public void setPlaylistIds(List<String> playlistIds) { this.playlistIds = playlistIds; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}