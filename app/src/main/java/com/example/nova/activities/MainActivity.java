// MainActivity.java
package com.example.nova.activities;

import android.os.Bundle;
import android.util.Log;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView songsRecyclerView;
    private DeezerTrackAdapter trackAdapter;
    private DeezerApiService apiService;
    private FirebaseService firebaseService;
    private List<String> userGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getInstance().getApiService();
        firebaseService = FirebaseService.getInstance();

        initRecyclerView();
        loadUserGenres();
    }

    private void initRecyclerView() {
        songsRecyclerView = findViewById(R.id.songsRecyclerView);
        trackAdapter = new DeezerTrackAdapter();
        trackAdapter.setDarkTheme(false);
        trackAdapter.setOnTrackClickListener(new DeezerTrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(DeezerTrack track, int position) {
                // Will implement player later
                Toast.makeText(MainActivity.this, "Playing: " + track.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLikeClick(DeezerTrack track, int position) {
                toggleLike(track, position);
            }
        });
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songsRecyclerView.setAdapter(trackAdapter);
    }

    private void loadUserGenres() {
        String userId = firebaseService.getCurrentUser().getUid();
        firebaseService.getUserData(userId, new FirebaseService.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                userGenres = user.getFavoriteGenres();
                if (userGenres != null && !userGenres.isEmpty()) {
                    loadRecommendationsByGenres();
                } else {
                    loadChartTracks(); // fallback to charts
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("MainActivity", "Error loading user: " + error);
                loadChartTracks(); // fallback
            }
        });
    }

    private void loadRecommendationsByGenres() {
        // For now, load chart tracks
        // In real implementation, you would call an API that filters by genres
        // Deezer API doesn't have direct genre recommendations, so we use charts as placeholder
        loadChartTracks();

        // TODO: Implement genre-based recommendations using Deezer's editorial playlists
        // or using a recommendation algorithm on the backend
    }

    private void loadChartTracks() {
        apiService.getChartTracks().enqueue(new retrofit2.Callback<DeezerApiService.ChartResponse>() {
            @Override
            public void onResponse(retrofit2.Call<DeezerApiService.ChartResponse> call,
                                   retrofit2.Response<DeezerApiService.ChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DeezerTrack> tracks = response.body().getTracks();
                    trackAdapter.setTracks(tracks);
                    Log.d("MainActivity", "Tracks loaded: " + tracks.size());
                } else {
                    Log.e("MainActivity", "Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DeezerApiService.ChartResponse> call, Throwable t) {
                Log.e("MainActivity", "Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike(DeezerTrack track, int position) {
        boolean newLikeState = !track.isLiked();
        track.setLiked(newLikeState);
        trackAdapter.notifyItemChanged(position);

        String userId = firebaseService.getCurrentUser().getUid();
        String trackId = String.valueOf(track.getId());

        if (newLikeState) {
            firebaseService.addLikedSong(userId, trackId, new FirebaseService.OnUpdateListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    track.setLiked(false);
                    trackAdapter.notifyItemChanged(position);
                }
            });
        } else {
            firebaseService.removeLikedSong(userId, trackId, new FirebaseService.OnUpdateListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    track.setLiked(true);
                    trackAdapter.notifyItemChanged(position);
                }
            });
        }
    }
}