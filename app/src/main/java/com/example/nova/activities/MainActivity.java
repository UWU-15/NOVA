package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova.R;
import com.example.nova.adapters.SongAdapter;
import com.example.nova.activities.cache.TrackCache;
import com.example.nova.models.Track;
import com.example.nova.activities.repository.MusicRepository;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView songsRecyclerView;
    private SongAdapter adapter;
    private MusicRepository repository;

    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new MusicRepository();

        if (!TrackCache.isEmpty()) {
            setContentView(R.layout.activity_main);
            initViews();
            initRecycler();
            adapter.setTracks(TrackCache.get());
            showContent();
        } else {
            setContentView(R.layout.activity_main);
            initViews();
            initRecycler();
            loadTracks();
        }
    }

    private void initViews() {
        songsRecyclerView = findViewById(R.id.songsRecyclerView);
        progress = findViewById(R.id.progress);
    }

    private void initRecycler() {
        adapter = new SongAdapter();
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songsRecyclerView.setAdapter(adapter);
    }

    private void loadTracks() {

        showLoading();

        repository.getTracks().enqueue(new Callback<List<Track>>() {

            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<Track> tracks = response.body();

                    TrackCache.save(tracks); // 💾 кеш

                    adapter.setTracks(tracks);

                    showContent();
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                showError();
            }
        });
    }

    private void showLoading() {
        progress.setVisibility(View.VISIBLE);
        songsRecyclerView.setVisibility(View.GONE);
    }

    private void showContent() {
        progress.setVisibility(View.GONE);
        songsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        progress.setVisibility(View.GONE);
        songsRecyclerView.setVisibility(View.VISIBLE);
    }
}