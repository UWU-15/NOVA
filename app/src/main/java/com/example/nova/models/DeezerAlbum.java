// com/example/nova/models/DeezerAlbum.java
package com.example.nova.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeezerAlbum {
    private long id;
    private String title;
    private String link;
    private String cover;
    @SerializedName("cover_small") private String coverSmall;
    @SerializedName("cover_medium") private String coverMedium;
    @SerializedName("cover_big") private String coverBig;
    @SerializedName("cover_xl") private String coverXl;
    @SerializedName("release_date") private String releaseDate;
    @SerializedName("tracklist") private String tracklistUrl;
    @SerializedName("nb_tracks") private int trackCount;
    private DeezerArtist artist;
    private List<DeezerTrack> tracks;

    public DeezerAlbum() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getCover() { return cover; }
    public String getCoverSmall() { return coverSmall; }
    public String getCoverMedium() { return coverMedium; }
    public String getCoverBig() { return coverBig; }
    public String getCoverXl() { return coverXl; }
    public String getReleaseDate() { return releaseDate; }
    public String getTracklistUrl() { return tracklistUrl; }
    public int getTrackCount() { return trackCount; }
    public DeezerArtist getArtist() { return artist; }
    public void setArtist(DeezerArtist artist) { this.artist = artist; }
    public List<DeezerTrack> getTracks() { return tracks; }
    public void setTracks(List<DeezerTrack> tracks) { this.tracks = tracks; }

    public String getArtistName() {
        return artist != null ? artist.getName() : "Unknown Artist";
    }
}