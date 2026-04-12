// services/MusicPlayerService.java
package com.example.nova.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.common.util.UnstableApi;

import com.example.nova.R;
import com.example.nova.activities.MainActivity;
import com.example.nova.models.DeezerTrack;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class MusicPlayerService extends Service {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new MusicBinder();
    private ExoPlayer exoPlayer;
    private List<DeezerTrack> playlist = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isPlaying = false;
    private OnMusicStateListener stateListener;

    public interface OnMusicStateListener {
        void onSongChanged(DeezerTrack track);
        void onPlayStateChanged(boolean isPlaying);
        void onProgressUpdated(int progress, int duration);
    }

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializePlayer();
        createNotificationChannel();
    }

    private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                isPlaying = playbackState == Player.STATE_READY && exoPlayer.isPlaying();
                if (stateListener != null) {
                    stateListener.onPlayStateChanged(isPlaying);
                }
                updateNotification();
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                if (currentIndex < playlist.size()) {
                    DeezerTrack track = playlist.get(currentIndex);
                    if (stateListener != null) {
                        stateListener.onSongChanged(track);
                    }
                    updateNotification();
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                // Handle discontinuity if needed
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music player controls");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void setPlaylist(List<DeezerTrack> tracks, int startIndex) {
        if (tracks == null || tracks.isEmpty()) return;

        this.playlist = new ArrayList<>(tracks);
        this.currentIndex = startIndex;
        playTrack(startIndex);
    }

    public void playTrack(int index) {
        if (index < 0 || index >= playlist.size()) return;

        currentIndex = index;
        DeezerTrack track = playlist.get(currentIndex);
        String previewUrl = track.getPreviewUrl();

        if (previewUrl != null && !previewUrl.isEmpty()) {
            try {
                MediaItem mediaItem = MediaItem.fromUri(previewUrl);
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
                isPlaying = true;

                if (stateListener != null) {
                    stateListener.onSongChanged(track);
                    stateListener.onPlayStateChanged(true);
                }

                // Start foreground service with notification
                startForeground(NOTIFICATION_ID, createNotification(track));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error playing track: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Preview not available for this track", Toast.LENGTH_SHORT).show();
        }
    }

    public void play() {
        if (exoPlayer != null && playlist != null && !playlist.isEmpty()) {
            exoPlayer.play();
            isPlaying = true;
            if (stateListener != null) {
                stateListener.onPlayStateChanged(true);
            }
            updateNotification();
        }
    }

    public void pause() {
        if (exoPlayer != null) {
            exoPlayer.pause();
            isPlaying = false;
            if (stateListener != null) {
                stateListener.onPlayStateChanged(false);
            }
            updateNotification();
        }
    }

    public void next() {
        if (currentIndex + 1 < playlist.size()) {
            playTrack(currentIndex + 1);
        } else {
            // Loop to first track if at end
            playTrack(0);
        }
    }

    public void previous() {
        if (currentIndex - 1 >= 0) {
            playTrack(currentIndex - 1);
        } else {
            // Go to last track if at beginning
            playTrack(playlist.size() - 1);
        }
    }

    public void stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            isPlaying = false;
            stopForeground(true);
            if (stateListener != null) {
                stateListener.onPlayStateChanged(false);
            }
        }
    }

    public void seekTo(int position) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(position);
        }
    }

    public DeezerTrack getCurrentTrack() {
        if (playlist != null && !playlist.isEmpty() && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }

    public int getCurrentPosition() {
        return exoPlayer != null ? (int) exoPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        if (exoPlayer != null && exoPlayer.getDuration() > 0) {
            return (int) exoPlayer.getDuration();
        }
        DeezerTrack track = getCurrentTrack();
        return track != null ? track.getDuration() * 1000 : 0;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public List<DeezerTrack> getPlaylist() {
        return playlist;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setStateListener(OnMusicStateListener listener) {
        this.stateListener = listener;
    }

    private Notification createNotification(DeezerTrack track) {
        if (track == null) {
            track = createUnknownTrack();
        }

        // Create intents for notification actions
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playIntent = new Intent(this, MusicPlayerService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, MusicPlayerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MusicPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, MusicPlayerService.class);
        prevIntent.setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, MusicPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get artist name safely
        String artistName = track.getArtistName();
        if (artistName == null) artistName = "Unknown Artist";

        // Build notification with media controls
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(track.getTitle() != null ? track.getTitle() : "Unknown Track")
                .setContentText(artistName)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(null))
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? "Pause" : "Play", isPlaying ? pausePendingIntent : playPendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent);

        return builder.build();
    }

    private DeezerTrack createUnknownTrack() {
        DeezerTrack track = new DeezerTrack();
        track.setId(-1);
        track.setTitle("Unknown Track");
        track.setDuration(0);
        track.setPreviewUrl(null);

        // Create and set artist using DeezerTrack.Artist inner class
        DeezerTrack.Artist artist = new DeezerTrack.Artist();
        artist.setName("Unknown Artist");
        track.setArtist(artist);

        // Create and set album
        DeezerTrack.Album album = new DeezerTrack.Album();
        album.setCoverMedium(null);
        track.setAlbum(album);

        return track;
    }

    private void updateNotification() {
        DeezerTrack track = getCurrentTrack();
        if (track != null) {
            Notification notification = createNotification(track);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_PLAY:
                    play();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_NEXT:
                    next();
                    break;
                case ACTION_PREVIOUS:
                    previous();
                    break;
                case ACTION_STOP:
                    stop();
                    break;
            }
        }

        // Start foreground with notification if we have a track
        DeezerTrack currentTrack = getCurrentTrack();
        if (currentTrack != null && isPlaying) {
            startForeground(NOTIFICATION_ID, createNotification(currentTrack));
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        stopForeground(true);
    }
}