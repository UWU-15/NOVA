package com.example.nova.api;

import com.example.nova.models.*;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("tracks")
    Call<List<Track>> getTracks();

    @GET("tracks")
    Call<List<Track>> search(@Query("title") String query);

    @GET("tracks")
    Call<List<Track>> getTracksByArtist(@Query("artist_id") String artistId);

    @GET("artists")
    Call<List<Artist>> getArtists();

    @GET("albums")
    Call<List<Album>> getAlbums();


    // получить лайкнутые треки
    @GET("favorites")
    Call<List<Favorite>> getFavorites(@Query("user_id") String userId);

    // добавить в избранное
    @POST("favorites")
    Call<Void> addFavorite(@Body Favorite favorite);

    // удалить из избранного
    @DELETE("favorites")
    Call<Void> removeFavorite(@Query("user_id") String userId,
                              @Query("track_id") String trackId);

    @GET("recommended_tracks")
    Call<List<Track>> getRecommendedTracks(@Query("user_id") String userId);

    @GET("recommended_artists")
    Call<List<Artist>> getRecommendedArtists(@Query("user_id") String userId);

    @GET("recommended_albums")
    Call<List<Album>> getRecommendedAlbums(@Query("user_id") String userId);
}