package com.example.nova.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeezerArtist {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("picture_medium")
    private String pictureMedium;

    @SerializedName("nb_album")
    private int albumCount;

    @SerializedName("nb_fan")
    private int fanCount;

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPictureMedium() { return pictureMedium; }
    public void setPictureMedium(String pictureMedium) { this.pictureMedium = pictureMedium; }

    public int getAlbumCount() { return albumCount; }
    public void setAlbumCount(int albumCount) { this.albumCount = albumCount; }

    public int getFanCount() { return fanCount; }
    public void setFanCount(int fanCount) { this.fanCount = fanCount; }
}