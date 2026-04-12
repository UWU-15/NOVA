// com/example/nova/api/DeezerApiService.java
package com.example.nova.api;

import com.example.nova.models.DeezerAlbum;
import com.example.nova.models.DeezerArtist;
import com.example.nova.models.DeezerTrack;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DeezerApiService {

    @GET("search/track")
    Call<SearchResponse> searchTracks(@Query("q") String query);

    @GET("track/{id}")
    Call<DeezerTrack> getTrack(@Path("id") long id);

    @GET("album/{id}")
    Call<DeezerAlbum> getAlbum(@Path("id") long id);

    @GET("artist/{id}")
    Call<DeezerArtist> getArtist(@Path("id") long id);

    @GET("artist/{id}/top")
    Call<TopTracksResponse> getArtistTopTracks(@Path("id") long id);

    @GET("chart/0/tracks")
    Call<ChartResponse> getChartTracks();

    @GET("search/album")
    Call<AlbumSearchResponse> searchAlbums(@Query("q") String query);

    @GET("search/artist")
    Call<ArtistSearchResponse> searchArtists(@Query("q") String query);

    @GET("genre/{id}/artists")
    Call<ArtistSearchResponse> getGenreArtists(@Path("id") int genreId);

    class SearchResponse {
        private List<DeezerTrack> data;
        private int total;
        public List<DeezerTrack> getData() { return data; }
        public int getTotal() { return total; }
    }

    class TopTracksResponse {
        private List<DeezerTrack> data;
        public List<DeezerTrack> getData() { return data; }
    }

    class ChartResponse {
        private List<DeezerTrack> tracks;
        private List<DeezerAlbum> albums;
        private List<DeezerArtist> artists;
        public List<DeezerTrack> getTracks() { return tracks; }
        public List<DeezerAlbum> getAlbums() { return albums; }
        public List<DeezerArtist> getArtists() { return artists; }
    }

    class AlbumSearchResponse {
        private List<DeezerAlbum> data;
        public List<DeezerAlbum> getData() { return data; }
    }

    class ArtistSearchResponse {
        private List<DeezerArtist> data;
        public List<DeezerArtist> getData() { return data; }
    }
}