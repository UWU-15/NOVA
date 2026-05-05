package com.example.nova.activities.repository;

import com.example.nova.api.ApiService;
import com.example.nova.api.RetrofitClient;
import com.example.nova.models.*;

import java.util.List;

import retrofit2.Call;

public class MusicRepository {

    private final ApiService api;

    public MusicRepository() {
        api = RetrofitClient.getInstance().create(ApiService.class);
    }

    public Call<List<Track>> getTracks() {
        return api.getTracks();
    }

    public Call<List<Track>> searchTracks(String query) {
        return api.searchTracks(query);
    }
    public Call<List<Track>> getLiked(String userId) {
        return api.getLikedTracks(userId);
    }
    public Call<List<Track>> getRecommendations(String userId) {
        return api.getRecommendedTracks(userId);
    }

    public Call<List<Artist>> getRecommendedArtists(String userId) {
        return api.getRecommendedArtists(userId);
    }

    public Call<List<Album>> getRecommendedAlbums(String userId) {
        return api.getRecommendedAlbums(userId);
    }
}