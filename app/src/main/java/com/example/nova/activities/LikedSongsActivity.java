package com.example.nova.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova.R;
import com.example.nova.adapters.SongAdapter;
import com.example.nova.api.ApiService;
import com.example.nova.api.RetrofitClient;
import com.example.nova.services.FirebaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikedSongsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView likedTitle;
    private RecyclerView likedRecyclerView;
    private TextView emptyState;

    private SongAdapter trackAdapter;

    private FirebaseService firebaseService;

    private final List<DeezerTrack> likedTracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_songs);

        firebaseService = FirebaseService.getInstance();

        initViews();
        setupRecycler();
        setupClickListeners();
        loadLikedSongs();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        likedTitle = findViewById(R.id.likedTitle);
        likedRecyclerView = findViewById(R.id.likedRecyclerView);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecycler() {
        trackAdapter = new SongAdapter();
        trackAdapter.setDarkTheme(true);

        trackAdapter.setOnTrackClickListener(new SongAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(DeezerTrack track, int position) {
                Toast.makeText(LikedSongsActivity.this,
                        "Play: " + track.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLikeClick(DeezerTrack track, int position) {
                removeLikedSong(track, position);
            }
        });

        likedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        likedRecyclerView.setAdapter(trackAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadLikedSongs() {
        if (firebaseService.getCurrentUser() == null) return;

        String userId = firebaseService.getCurrentUser().getUid();

        firebaseService.getUserData(userId, new FirebaseService.OnUserDataListener() {
            @Override
            public void onSuccess(Map<String, Object> userData) {

                List<String> likedSongIds = (List<String>) userData.get("likedSongs");

                likedTracks.clear();

                if (likedSongIds == null || likedSongIds.isEmpty()) {
                    updateEmptyState(true);
                    return;
                }

                updateEmptyState(false);

                for (String id : likedSongIds) {
                    fetchTrack(id);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LikedSongsActivity.this,
                        "Error loading: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrack(String trackId) {
        ApiService api = RetrofitClient.getInstance().getApiService();

        try {
            api.getTrack(Long.parseLong(trackId)).enqueue(new Callback<DeezerTrack>() {
                @Override
                public void onResponse(Call<DeezerTrack> call, Response<DeezerTrack> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        DeezerTrack track = response.body();
                        track.setLiked(true);

                        // защита от дублей
                        for (DeezerTrack t : likedTracks) {
                            if (t.getId() == track.getId()) return;
                        }

                        likedTracks.add(track);
                        trackAdapter.setTracks(new ArrayList<>(likedTracks));
                    }
                }

                @Override
                public void onFailure(Call<DeezerTrack> call, Throwable t) {
                    // можно логировать
                }
            });

        } catch (Exception ignored) {}
    }

    private void removeLikedSong(DeezerTrack track, int position) {
        if (firebaseService.getCurrentUser() == null) return;

        String userId = firebaseService.getCurrentUser().getUid();
        String trackId = String.valueOf(track.getId());

        firebaseService.removeLikedSong(userId, trackId, new FirebaseService.OnUpdateListener() {
            @Override
            public void onSuccess() {

                if (position >= 0 && position < likedTracks.size()) {
                    likedTracks.remove(position);
                }

                trackAdapter.setTracks(new ArrayList<>(likedTracks));

                updateEmptyState(likedTracks.isEmpty());

                Toast.makeText(LikedSongsActivity.this,
                        "Removed",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LikedSongsActivity.this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyState.setVisibility(View.VISIBLE);
            likedRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            likedRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}