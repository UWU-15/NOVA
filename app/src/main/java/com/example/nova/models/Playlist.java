// Playlist.java
package com.example.nova.models;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String id;
    private String name;
    private String userId;
    private List<Long> trackIds;
    private String coverUrl;
    private long createdAt;

    public Playlist() {
        // Empty constructor for Firestore
    }

    public Playlist(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.trackIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Long> getTrackIds() { return trackIds; }
    public void setTrackIds(List<Long> trackIds) { this.trackIds = trackIds; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public void addTrack(long trackId) {
        if (trackIds == null) {
            trackIds = new ArrayList<>();
        }
        trackIds.add(trackId);
    }

    public void removeTrack(long trackId) {
        if (trackIds != null) {
            trackIds.remove(trackId);
        }
    }

    public int getTrackCount() {
        return trackIds != null ? trackIds.size() : 0;
    }
}