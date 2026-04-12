// FullPlayerActivity.java
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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.nova.R;
import com.example.nova.models.DeezerTrack;
import com.example.nova.services.MusicPlayerService;
import java.util.List;

public class FullPlayerActivity extends AppCompatActivity {

    private ImageButton btnBack, btnLike, btnMenuMore, btnRepeat, btnPrev, btnNext, btnShuffle;
    private ImageButton btnFullPlayPause;
    private ImageView ivFullArt, albumGlow;
    private TextView tvFullTitle, tvFullArtist, tvCurrentTime, tvTotalTime;
    private SeekBar playerSeekBar;

    private MusicPlayerService musicService;
    private boolean isBound = false;
    private Handler progressHandler = new Handler();
    private Runnable progressRunnable;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            updateUI();
            setupProgressUpdates();
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
        setupClickListeners();

        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnLike);
        btnMenuMore = findViewById(R.id.btnMenuMore);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnFullPlayPause = findViewById(R.id.btnFullPlayPause);
        ivFullArt = findViewById(R.id.ivFullArt);
        albumGlow = findViewById(R.id.albumGlow);
        tvFullTitle = findViewById(R.id.tvFullTitle);
        tvFullArtist = findViewById(R.id.tvFullArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        playerSeekBar = findViewById(R.id.playerSeekBar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFullPlayPause.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                    btnFullPlayPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    musicService.play();
                    btnFullPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.previous();
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.next();
                updateUI();
            }
        });

        btnLike.setOnClickListener(v -> {
            if (musicService != null) {
                DeezerTrack track = musicService.getCurrentTrack();
                if (track != null) {
                    track.setLiked(!track.isLiked());
                    updateLikeButton();
                    Toast.makeText(this, track.isLiked() ? "Added to favorites" : "Removed from favorites",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        if (musicService != null) {
            DeezerTrack currentTrack = musicService.getCurrentTrack();
            if (currentTrack != null) {
                tvFullTitle.setText(currentTrack.getTitle());
                tvFullArtist.setText(currentTrack.getArtistName());
                tvTotalTime.setText(currentTrack.getFormattedDuration());
                updateLikeButton();

                // Load album art
                if (currentTrack.getAlbumCoverUrl() != null) {
                    Glide.with(this)
                            .load(currentTrack.getAlbumCoverUrl())
                            .into(ivFullArt);
                }

                int duration = musicService.getDuration();
                playerSeekBar.setMax(duration);
                playerSeekBar.setProgress(musicService.getCurrentPosition());
                tvCurrentTime.setText(formatTime(musicService.getCurrentPosition() / 1000));

                // Update play/pause button
                if (musicService.isPlaying()) {
                    btnFullPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    btnFullPlayPause.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        }
    }

    private void updateLikeButton() {
        if (musicService != null) {
            DeezerTrack track = musicService.getCurrentTrack();
            if (track != null) {
                btnLike.setImageResource(track.isLiked() ? R.drawable.ic_liked : R.drawable.ic_like);
            }
        }
    }

    private void setupProgressUpdates() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicService != null && musicService.isPlaying()) {
                    int currentPosition = musicService.getCurrentPosition();
                    playerSeekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition / 1000));
                }
                progressHandler.postDelayed(this, 1000);
            }
        };
        progressHandler.post(progressRunnable);
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
        progressHandler.removeCallbacks(progressRunnable);
    }
}