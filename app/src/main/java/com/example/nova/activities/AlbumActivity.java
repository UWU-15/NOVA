// AlbumActivity.java
package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nova.R;
import com.example.nova.adapters.SongAdapter;
import com.example.nova.api.ApiService;
import com.example.nova.api.RetrofitClient;
import com.example.nova.services.MusicPlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivAlbumCover;
    private TextView tvAlbumName;
    private RecyclerView rvAlbumTracks;
    private FloatingActionButton fabPlayAlbum;
    private SongAdapter trackAdapter;
    private ApiService apiService;
    private DeezerAlbum album;
    private List<DeezerTrack> tracks;
    private long albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        apiService = RetrofitClient.getInstance().getApiService();
        albumId = getIntent().getLongExtra("album_id", 0);

        initViews();
        setupClickListeners();
        loadAlbumData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAlbumCover = findViewById(R.id.ivAlbumCover);
        tvAlbumName = findViewById(R.id.tvAlbumName);
        rvAlbumTracks = findViewById(R.id.rvAlbumTracks);
        fabPlayAlbum = findViewById(R.id.fabPlayAlbum);

        trackAdapter = new SongAdapter();
        trackAdapter.setDarkTheme(true);
        trackAdapter.setOnTrackClickListener(new SongAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(DeezerTrack track, int position) {
                startMusicPlayer(position);
            }

            @Override
            public void onLikeClick(DeezerTrack track, int position) {
                track.setLiked(!track.isLiked());
                trackAdapter.notifyItemChanged(position);
            }
        });

        rvAlbumTracks.setLayoutManager(new LinearLayoutManager(this));
        rvAlbumTracks.setAdapter(trackAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        fabPlayAlbum.setOnClickListener(v -> {
            if (tracks != null && !tracks.isEmpty()) {
                startMusicPlayer(0);
            }
        });
    }

    private void loadAlbumData() {
        apiService.getAlbum(albumId).enqueue(new Callback<DeezerAlbum>() {
            @Override
            public void onResponse(Call<DeezerAlbum> call, Response<DeezerAlbum> response) {
                if (response.isSuccessful() && response.body() != null) {
                    album = response.body();
                    tvAlbumName.setText(album.getTitle());

                    // Load album cover
                    if (album.getCoverMedium() != null) {
                        Glide.with(AlbumActivity.this)
                                .load(album.getCoverMedium())
                                .into(ivAlbumCover);
                    }

                    // Load tracks
                    loadAlbumTracks();
                }
            }

            @Override
            public void onFailure(Call<DeezerAlbum> call, Throwable t) {
                Toast.makeText(AlbumActivity.this, "Error loading album: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAlbumTracks() {
        // Deezer API doesn't directly return tracks with album, need to fetch tracklist
        // For demo, we'll load chart tracks
        apiService.getChartTracks().enqueue(new Callback<ApiService.ChartResponse>() {
            @Override
            public void onResponse(Call<ApiService.ChartResponse> call,
                                   Response<ApiService.ChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tracks = response.body().getTracks();
                    if (tracks != null) {
                        trackAdapter.setTracks(tracks);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ChartResponse> call, Throwable t) {
                Toast.makeText(AlbumActivity.this, "Error loading tracks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMusicPlayer(int position) {
        if (tracks == null || tracks.isEmpty()) return;

        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        startService(serviceIntent);

        Intent intent = new Intent(AlbumActivity.this, FullPlayerActivity.class);
        intent.putExtra("playlist_position", position);
        startActivity(intent);
    }
}