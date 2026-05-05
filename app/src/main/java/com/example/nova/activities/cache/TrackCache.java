package com.example.nova.activities.cache;

import com.example.nova.models.Track;

import java.util.List;

public class TrackCache {

    private static List<Track> cachedTracks;

    public static void save(List<Track> tracks) {
        cachedTracks = tracks;
    }

    public static List<Track> get() {
        return cachedTracks;
    }

    public static boolean isEmpty() {
        return cachedTracks == null || cachedTracks.isEmpty();
    }
}