// SearchActivity.java
package com.example.nova.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nova.R;
import com.example.nova.adapters.SongAdapter;
import com.example.nova.api.ApiService;
import com.example.nova.api.RetrofitClient;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView searchTitle;
    private EditText searchEditText;
    private RecyclerView searchRecyclerView;
    private SongAdapter trackAdapter;
    private ApiService apiService;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        apiService = RetrofitClient.getInstance().getApiService();
        initViews();
        setupClickListeners();
        setupSearch();
    }

    private void initViews() {
        backButton = findViewById(R.id.btnBack);
        searchTitle = findViewById(R.id.searchTitle);
        searchEditText = findViewById(R.id.searchEditText);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);

        trackAdapter = new SongAdapter();
        trackAdapter.setDarkTheme(true);
        trackAdapter.setOnTrackClickListener(new SongAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(DeezerTrack track, int position) {
                // Start player with search results
                Toast.makeText(SearchActivity.this, "Playing: " + track.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLikeClick(DeezerTrack track, int position) {
                track.setLiked(!track.isLiked());
                trackAdapter.notifyItemChanged(position);
            }
        });

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(trackAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            trackAdapter.setTracks(null);
            return;
        }

        apiService.searchTracks(query).enqueue(new Callback<ApiService.SearchResponse>() {
            @Override
            public void onResponse(Call<ApiService.SearchResponse> call,
                                   Response<ApiService.SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DeezerTrack> tracks = response.body().getData();
                    trackAdapter.setTracks(tracks);
                }
            }

            @Override
            public void onFailure(Call<ApiService.SearchResponse> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Search failed: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}