package com.example.nova.activities;

import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nova.R;
import com.example.nova.api.ApiService;
import com.example.nova.api.RetrofitClient;
import com.example.nova.services.MusicPlayerService;

import java.util.ArrayList;
import java.util.List;

public class FullPlayerActivity extends AppCompatActivity {

    private ImageButton btnBack, btnPrev, btnNext, btnPlayPause, btnLike;
    private ImageView ivArtwork;
    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;

    private MusicPlayerService musicService;
    private boolean isBound = false;

    private final Handler handler = new Handler();

    private final List<DeezerTrack> playlist = new ArrayList<>();
    private int currentIndex = 0;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            if (!playlist.isEmpty()) {
                musicService.setPlaylist(playlist, currentIndex);
            }

            updateUI();
            startProgress();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        initViews();
        setupListeners();
        setupSeekBar();
        loadTrack();

        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnPlayPause = findViewById(R.id.btnFullPlayPause);
        btnLike = findViewById(R.id.btnLike);

        ivArtwork = findViewById(R.id.ivFullArt);

        tvTitle = findViewById(R.id.tvFullTitle);
        tvArtist = findViewById(R.id.tvFullArtist);

        seekBar = findViewById(R.id.playerSeekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (musicService == null) return;

            if (musicService.isPlaying()) {
                musicService.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play);
            } else {
                musicService.play();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });

        btnNext.setOnClickListener(v -> playNext());
        btnPrev.setOnClickListener(v -> playPrev());

        btnLike.setOnClickListener(v -> {
            if (musicService == null) return;

            DeezerTrack track = musicService.getCurrentTrack();
            if (track == null) return;

            track.setLiked(!track.isLiked());
            updateLikeIcon();
        });
    }

    private void playNext() {
        if (playlist.isEmpty()) return;

        if (currentIndex < playlist.size() - 1) {
            currentIndex++;
            musicService.playTrack(currentIndex);
            updateUI();
        }
    }

    private void playPrev() {
        if (playlist.isEmpty()) return;

        if (currentIndex > 0) {
            currentIndex--;
            musicService.playTrack(currentIndex);
            updateUI();
        }
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadTrack() {
        ApiService api = RetrofitClient.getInstance().getApiService();

        api.getTrack(123).enqueue(new retrofit2.Callback<DeezerTrack>() {
            @Override
            public void onResponse(retrofit2.Call<DeezerTrack> call,
                                   retrofit2.Response<DeezerTrack> response) {

                if (response.body() != null) {
                    playlist.add(response.body());
                    updateUI();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DeezerTrack> call, Throwable t) {}
        });
    }

    private void updateUI() {
        if (musicService == null || musicService.getCurrentTrack() == null) return;

        DeezerTrack track = musicService.getCurrentTrack();

        tvTitle.setText(track.getTitle());
        tvArtist.setText(track.getArtistName());

        Glide.with(this)
                .load(track.getAlbumCoverUrl())
                .into(ivArtwork);

        updateLikeIcon();

        seekBar.setMax(musicService.getDuration());
        tvTotalTime.setText(formatTime(musicService.getDuration() / 1000));
    }

    private void updateLikeIcon() {
        DeezerTrack track = musicService.getCurrentTrack();

        btnLike.setImageResource(
                track.isLiked()
                        ? R.drawable.ic_liked
                        : R.drawable.ic_like
        );
    }

    private void startProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int pos = musicService.getCurrentPosition();
                    seekBar.setProgress(pos);
                    tvCurrentTime.setText(formatTime(pos / 1000));
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private String formatTime(int sec) {
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) unbindService(connection);
        handler.removeCallbacksAndMessages(null);
    }
}