// PlaylistViewActivity.java
package com.example.nova.activities;

import android.content.Intent;
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
import com.example.nova.models.Playlist;
import com.example.nova.models.User;
import com.example.nova.services.FirebaseService;
import com.example.nova.services.MusicPlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistViewActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvPlaylistName;
    private RecyclerView rvSongsInPlaylist;
    private FloatingActionButton btnPlay;
    private DeezerTrackAdapter trackAdapter;
    private String playlistId;
    private String playlistName;
    private List<DeezerTrack> tracks = new ArrayList<>();
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_view);

        firebaseService = FirebaseService.getInstance();

        // Get playlist data from intent
        playlistId = getIntent().getStringExtra("playlist_id");
        playlistName = getIntent().getStringExtra("playlist_name");

        initViews();
        setupClickListeners();
        loadPlaylistTracks();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPlaylistName = findViewById(R.id.tvPlaylistName);
        rvSongsInPlaylist = findViewById(R.id.rvSongsInPlaylist);
        btnPlay = findViewById(R.id.btnPlay);

        tvPlaylistName.setText(playlistName != null ? playlistName : "Playlist");

        trackAdapter = new DeezerTrackAdapter();
        trackAdapter.setDarkTheme(false);
        trackAdapter.setOnTrackClickListener(new DeezerTrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(DeezerTrack track, int position) {
                // Start player with this track and playlist
                playTrack(position);
            }

            @Override
            public void onLikeClick(DeezerTrack track, int position) {
                toggleLike(track, position);
            }
        });

        rvSongsInPlaylist.setLayoutManager(new LinearLayoutManager(this));
        rvSongsInPlaylist.setAdapter(trackAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlay.setOnClickListener(v -> {
            if (!tracks.isEmpty()) {
                playTrack(0);
            } else {
                Toast.makeText(this, "No tracks in this playlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlaylistTracks() {
        String userId = firebaseService.getCurrentUser().getUid();

        firebaseService.getUserPlaylists(userId, new FirebaseService.OnPlaylistsListener() {
            @Override
            public void onSuccess(List<Playlist> playlists) {
                Playlist targetPlaylist = null;
                for (Playlist playlist : playlists) {
                    if (playlist.getId().equals(playlistId)) {
                        targetPlaylist = playlist;
                        break;
                    }
                }

                if (targetPlaylist != null) {
                    List<Long> trackIds = targetPlaylist.getTrackIds();
                    if (trackIds != null && !trackIds.isEmpty()) {
                        tracks.clear();
                        for (Long trackId : trackIds) {
                            fetchTrack(trackId);
                        }
                    } else {
                        Toast.makeText(PlaylistViewActivity.this, "No tracks in this playlist", Toast.LENGTH_SHORT).show();
                        trackAdapter.setTracks(tracks);
                    }
                } else {
                    Toast.makeText(PlaylistViewActivity.this, "Playlist not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PlaylistViewActivity.this, "Error loading playlist: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrack(long trackId) {
        DeezerApiService apiService = RetrofitClient.getInstance().getApiService();
        apiService.getTrack(trackId).enqueue(new Callback<DeezerTrack>() {
            @Override
            public void onResponse(Call<DeezerTrack> call, Response<DeezerTrack> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeezerTrack track = response.body();
                    checkIfTrackIsLiked(track);
                    tracks.add(track);
                    trackAdapter.setTracks(new ArrayList<>(tracks));
                }
            }

            @Override
            public void onFailure(Call<DeezerTrack> call, Throwable t) {
                Toast.makeText(PlaylistViewActivity.this, "Error loading track: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfTrackIsLiked(DeezerTrack track) {
        String userId = firebaseService.getCurrentUser().getUid();
        String trackId = String.valueOf(track.getId());

        firebaseService.getUserData(userId, new FirebaseService.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                List<String> likedSongs = user.getLikedSongs();
                if (likedSongs != null && likedSongs.contains(trackId)) {
                    track.setLiked(true);
                    // Refresh adapter to show updated like status
                    trackAdapter.setTracks(new ArrayList<>(tracks));
                }
            }

            @Override
            public void onFailure(String error) {
                // Ignore error, track remains not liked
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
                    Toast.makeText(PlaylistViewActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(PlaylistViewActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    track.setLiked(false);
                    trackAdapter.notifyItemChanged(position);
                }
            });
        } else {
            firebaseService.removeLikedSong(userId, trackId, new FirebaseService.OnUpdateListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(PlaylistViewActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(PlaylistViewActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    track.setLiked(true);
                    trackAdapter.notifyItemChanged(position);
                }
            });
        }
    }

    private void playTrack(int position) {
        if (tracks == null || tracks.isEmpty()) {
            Toast.makeText(this, "No tracks to play", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start music service
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        startService(serviceIntent);

        // Start full player activity with playlist data
        Intent intent = new Intent(PlaylistViewActivity.this, FullPlayerActivity.class);
        intent.putExtra("playlist_position", position);

        // Pass the entire playlist as serializable
        intent.putExtra("playlist_tracks", new ArrayList<>(tracks));
        intent.putExtra("playlist_name", playlistName);
        intent.putExtra("from_playlist", true);

        startActivity(intent);
    }
}