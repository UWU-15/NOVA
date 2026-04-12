// LikedSongsActivity.java
package com.example.nova.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nova.R;
import com.example.nova.adapters.DeezerTrackAdapter;
import com.example.nova.api.DeezerApiService;
import com.example.nova.api.RetrofitClient;
import com.example.nova.models.DeezerTrack;
import com.example.nova.models.User;
import com.example.nova.services.FirebaseService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikedSongsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView likedTitle;
    private RecyclerView likedRecyclerView;
    private DeezerTrackAdapter trackAdapter;
    private FirebaseService firebaseService;
    private List<DeezerTrack> likedTracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_songs);

        firebaseService = FirebaseService.getInstance();
        initViews();
        setupClickListeners();
        loadLikedSongs();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        likedTitle = findViewById(R.id.likedTitle);
        likedRecyclerView = findViewById(R.id.likedRecyclerView);

        trackAdapter = new DeezerTrackAdapter();
        trackAdapter.setDarkTheme(true);
        trackAdapter.setOnTrackClickListener(new DeezerTrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(DeezerTrack track, int position) {
                Toast.makeText(LikedSongsActivity.this, "Playing: " + track.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLikeClick(DeezerTrack track, int position) {
                // Unlike the song
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
        String userId = firebaseService.getCurrentUser().getUid();
        firebaseService.getUserData(userId, new FirebaseService.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                List<String> likedSongIds = user.getLikedSongs();
                likedTracks.clear();

                if (likedSongIds != null && !likedSongIds.isEmpty()) {
                    // For each liked song ID, fetch from Deezer API
                    for (String songId : likedSongIds) {
                        fetchTrackDetails(songId);
                    }
                } else {
                    trackAdapter.setTracks(likedTracks);
                    Toast.makeText(LikedSongsActivity.this, "No liked songs yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LikedSongsActivity.this, "Error loading liked songs: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrackDetails(String trackId) {
        DeezerApiService apiService = RetrofitClient.getInstance().getApiService();
        apiService.getTrack(Long.parseLong(trackId)).enqueue(new Callback<DeezerTrack>() {
            @Override
            public void onResponse(Call<DeezerTrack> call, Response<DeezerTrack> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeezerTrack track = response.body();
                    track.setLiked(true);
                    likedTracks.add(track);
                    trackAdapter.setTracks(likedTracks);
                }
            }

            @Override
            public void onFailure(Call<DeezerTrack> call, Throwable t) {
                // Handle error
                Toast.makeText(LikedSongsActivity.this, "Error loading track: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeLikedSong(DeezerTrack track, int position) {
        String userId = firebaseService.getCurrentUser().getUid();
        String trackId = String.valueOf(track.getId()); // Convert long to String

        firebaseService.removeLikedSong(userId, trackId, new FirebaseService.OnUpdateListener() {
            @Override
            public void onSuccess() {
                // Remove from local list and update UI
                likedTracks.remove(position);
                trackAdapter.setTracks(likedTracks);
                Toast.makeText(LikedSongsActivity.this, "Removed from favorites",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LikedSongsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}