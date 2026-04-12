// BlindPlayerActivity.java
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            updateUI();
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
        setupProgressUpdates();
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
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnBlindPlayPause.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                    btnBlindPlayPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    musicService.play();
                    btnBlindPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        btnBlindLike.setOnClickListener(v -> {
            if (musicService != null) {
                // Toggle like
                btnBlindLike.setColorFilter(0xFFA600FF);
            }
        });

        btnBlindDislike.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.next();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (musicService != null && musicService.getCurrentTrack() != null) {
            tvFullTitle.setText(musicService.getCurrentTrack().getTitle());
            tvFullArtist.setText(musicService.getCurrentTrack().getArtistName());
            tvTotalTime.setText(musicService.getCurrentTrack().getFormattedDuration());

            int duration = musicService.getDuration();
            playerSeekBar.setMax(duration);
            playerSeekBar.setProgress(musicService.getCurrentPosition());
            tvCurrentTime.setText(formatTime(musicService.getCurrentPosition() / 1000));

            if (musicService.isPlaying()) {
                btnBlindPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                btnBlindPlayPause.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    private void setupProgressUpdates() {
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (musicService != null && musicService.isPlaying()) {
                    int currentPosition = musicService.getCurrentPosition();
                    playerSeekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition / 1000));
                }
                progressHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
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