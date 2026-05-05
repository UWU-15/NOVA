package com.example.nova.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nova.R;
import com.example.nova.services.MusicPlayerService;

public class BlindPlayerActivity extends AppCompatActivity {

    private ImageButton btnBack, btnBlindDislike, btnBlindLike, btnBlindPlayPause;
    private ImageView ivFullArt, albumFrame, albumGlow;
    private TextView tvFullTitle, tvFullArtist, tvCurrentTime, tvTotalTime;
    private SeekBar playerSeekBar;

    private MusicPlayerService musicService;
    private boolean isBound = false;

    private Handler progressHandler = new Handler();

    private boolean isRevealed = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            loadTrack();
            startProgressLoop();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_player);

        initViews();
        setupClickListeners();

        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBlindDislike = findViewById(R.id.btnBlindDislike);
        btnBlindLike = findViewById(R.id.btnBlindLike);
        btnBlindPlayPause = findViewById(R.id.btnBlindPlayPause);

        ivFullArt = findViewById(R.id.ivFullArt);
        albumFrame = findViewById(R.id.albumFrame);
        albumGlow = findViewById(R.id.albumGlow);

        tvFullTitle = findViewById(R.id.tvFullTitle);
        tvFullArtist = findViewById(R.id.tvFullArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);

        playerSeekBar = findViewById(R.id.playerSeekBar);

        hideTrackInfo();
    }

    private void setupClickListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnBlindPlayPause.setOnClickListener(v -> {
            if (musicService == null) return;

            if (musicService.isPlaying()) {
                musicService.pause();
                btnBlindPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                musicService.play();
                btnBlindPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        btnBlindLike.setOnClickListener(v -> {
            if (musicService == null) return;

            musicService.likeCurrentTrack();

            revealTrackInfo();
        });

        btnBlindDislike.setOnClickListener(v -> {
            if (musicService == null) return;

            musicService.dislikeCurrentTrack();
            nextTrack();
        });
    }

    private void loadTrack() {
        if (musicService == null) return;

        musicService.prepareNextFromTags();

        updateSeekBar();
        updatePlayButton();
    }

    private void nextTrack() {
        if (musicService == null) return;

        isRevealed = false;
        hideTrackInfo();

        musicService.next();
        loadTrack();
    }

    private void revealTrackInfo() {
        if (musicService == null || musicService.getCurrentTrack() == null) return;

        isRevealed = true;

        tvFullTitle.setText(musicService.getCurrentTrack().getTitle());
        tvFullArtist.setText(musicService.getCurrentTrack().getArtistName());
        tvTotalTime.setText(musicService.getCurrentTrack().getFormattedDuration());

        ivFullArt.setVisibility(ImageView.VISIBLE);
        albumFrame.setAlpha(1f);
        albumGlow.setAlpha(0.8f);
    }

    private void hideTrackInfo() {
        tvFullTitle.setText("?");
        tvFullArtist.setText("Unknown");

        ivFullArt.setVisibility(ImageView.INVISIBLE);
        albumFrame.setAlpha(0.4f);
        albumGlow.setAlpha(0.3f);
    }

    private void updateSeekBar() {
        if (musicService == null) return;

        playerSeekBar.setMax(musicService.getDuration());
        playerSeekBar.setProgress(musicService.getCurrentPosition());
    }

    private void updatePlayButton() {
        if (musicService == null) return;

        btnBlindPlayPause.setImageResource(
                musicService.isPlaying()
                        ? android.R.drawable.ic_media_pause
                        : android.R.drawable.ic_media_play
        );
    }

    private void startProgressLoop() {
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int pos = musicService.getCurrentPosition();
                    playerSeekBar.setProgress(pos);
                    tvCurrentTime.setText(formatTime(pos / 1000));
                }
                progressHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBound) {
            unbindService(serviceConnection);
        }

        progressHandler.removeCallbacksAndMessages(null);
    }
}