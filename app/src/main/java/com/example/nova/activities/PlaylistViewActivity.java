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
import com.example.nova.adapters.SongAdapter;
import com.example.nova.models.Track;
import com.example.nova.services.MusicPlayerService;
import com.example.nova.supabase.SupabaseClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvPlaylistName;
    private RecyclerView rvSongsInPlaylist;

    private SongAdapter adapter;

    private String playlistId;
    private String playlistName;

    private final List<Track> tracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_view);

        playlistId = getIntent().getStringExtra("playlist_id");
        playlistName = getIntent().getStringExtra("playlist_name");

        initViews();
        setupListeners();
        loadPlaylist();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPlaylistName = findViewById(R.id.tvPlaylistName);
        rvSongsInPlaylist = findViewById(R.id.rvSongsInPlaylist);

        tvPlaylistName.setText(playlistName != null ? playlistName : "Playlist");

        adapter = new SongAdapter();
        adapter.setDarkTheme(false);

        adapter.setOnTrackClickListener(new SongAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(Track track, int position) {
                playTrack(position);
            }

            @Override
            public void onLikeClick(Track track, int position) {
                toggleLike(track, position);
            }
        });

        rvSongsInPlaylist.setLayoutManager(new LinearLayoutManager(this));
        rvSongsInPlaylist.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    // =========================
    // SUPABASE LOAD
    // =========================
    private void loadPlaylist() {
        SupabaseClient.getInstance()
                .getPlaylistTracks(playlistId, new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            JSONArray arr = response.getJSONArray("tracks");

                            tracks.clear();

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);

                                Track track = new Track();
                                track.setId(obj.getString("id"));
                                track.setTitle(obj.getString("title"));
                                track.setArtist(obj.optString("artist"));
                                track.setAudioUrl(obj.getString("audio_url"));
                                track.setCoverUrl(obj.optString("cover_url"));
                                track.setLiked(obj.optBoolean("liked", false));

                                tracks.add(track);
                            }

                            adapter.setTracks(tracks);

                        } catch (Exception e) {
                            Toast.makeText(PlaylistViewActivity.this,
                                    "Parse error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(PlaylistViewActivity.this,
                                "Failed to load playlist",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =========================
    // LIKE (SUPABASE)
    // =========================
    private void toggleLike(Track track, int position) {
        boolean newState = !track.isLiked();
        track.setLiked(newState);
        adapter.notifyItemChanged(position);

        SupabaseClient.getInstance()
                .setLike(track.getId(), newState, new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {}

                    @Override
                    public void onError(String error) {}
                });
    }

    // =========================
    // PLAY
    // =========================
    private void playTrack(int position) {
        if (tracks.isEmpty()) return;

        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent);

        Intent openPlayer = new Intent(this, FullPlayerActivity.class);
        openPlayer.putExtra("tracks", new ArrayList<>(tracks));
        openPlayer.putExtra("index", position);

        startActivity(openPlayer);
    }
}