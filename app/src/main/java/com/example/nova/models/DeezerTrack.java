// DeezerTrack.java
package com.example.nova.models;

import com.google.gson.annotations.SerializedName;

public class DeezerTrack {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("duration")
    private int duration;

    @SerializedName("preview")
    private String previewUrl;

    @SerializedName("artist")
    private Artist artist;

    @SerializedName("album")
    private Album album;

    private boolean isLiked;

    // Inner class for Artist
    public static class Artist {
        @SerializedName("id")
        private long id;

        @SerializedName("name")
        private String name;

        public long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Inner class for Album
    public static class Album {
        @SerializedName("id")
        private long id;

        @SerializedName("title")
        private String title;

        @SerializedName("cover_medium")
        private String coverMedium;

        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getCoverMedium() { return coverMedium; }
        public void setCoverMedium(String coverMedium) { this.coverMedium = coverMedium; }
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getPreviewUrl() { return previewUrl; }

    // Setters (added for MusicPlayerService)
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    public void setArtist(Artist artist) { this.artist = artist; }
    public void setAlbum(Album album) { this.album = album; }

    public String getArtistName() {
        return artist != null ? artist.getName() : "Unknown Artist";
    }

    public long getArtistId() {
        return artist != null ? artist.getId() : 0;
    }

    public Artist getArtist() { return artist; }

    public String getAlbumTitle() {
        return album != null ? album.getTitle() : "";
    }

    public String getAlbumCoverUrl() {
        return album != null ? album.getCoverMedium() : null;
    }

    public long getAlbumId() {
        return album != null ? album.getId() : 0;
    }

    public Album getAlbum() { return album; }

    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Like status
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
}