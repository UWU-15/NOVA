package com.example.nova.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nova.R;
import com.example.nova.adapters.PlaylistAdapter;
import com.example.nova.services.FirebaseService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import java.util.HashMap; // Добавлено
import java.util.Map;    // Добавлено

public class LibraryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView btnAddPlaylist;
    private RecyclerView rvPlaylists;
    private PlaylistAdapter playlistAdapter;
    private FirebaseService firebaseService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        firebaseService = FirebaseService.getInstance();
        if (firebaseService.getCurrentUser() != null) {
            userId = firebaseService.getCurrentUser().getUid();
        }

        initViews();
        setupClickListeners();
        loadPlaylists();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddPlaylist = findViewById(R.id.btnAddPlaylist);
        rvPlaylists = findViewById(R.id.rvPlaylists);

        playlistAdapter = new PlaylistAdapter();
        playlistAdapter.setOnPlaylistClickListener(new PlaylistAdapter.OnPlaylistClickListener() {
            @Override
            public void onPlaylistClick(Playlist playlist) {
                Intent intent = new Intent(LibraryActivity.this, PlaylistViewActivity.class);
                intent.putExtra("playlist_id", playlist.getId());
                intent.putExtra("playlist_name", playlist.getName());
                startActivity(intent);
            }

            @Override
            public void onPlaylistOptionsClick(Playlist playlist, View view) {
                showPlaylistOptionsDialog(playlist);
            }
        });

        rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        rvPlaylists.setAdapter(playlistAdapter);
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnAddPlaylist != null) btnAddPlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    private void loadPlaylists() {
        firebaseService.getUserPlaylists(userId, new FirebaseService.OnPlaylistsListener() {
            @Override
            public void onSuccess(List<Playlist> playlists) {
                playlistAdapter.setPlaylists(playlists);
            }

            @Override
            public void onFailure(Exception e) { // Изменено со String на Exception
                Toast.makeText(LibraryActivity.this, "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreatePlaylistDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_playlist, null);
        TextInputEditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);
        android.widget.ImageButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        android.widget.Button btnCreatePlaylist = dialogView.findViewById(R.id.btnCreatePlaylist);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        if (btnCreatePlaylist != null) {
            btnCreatePlaylist.setOnClickListener(v -> {
                String name = etPlaylistName.getText().toString().trim();
                if (!name.isEmpty()) {
                    firebaseService.createPlaylist(userId, name, new FirebaseService.OnPlaylistListener() {
                        @Override
                        public void onSuccess() { // В FirebaseService OnPlaylistListener.onSuccess() без параметров
                            loadPlaylists(); // Перезагружаем список
                            dialog.dismiss();
                            Toast.makeText(LibraryActivity.this, "Playlist created", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) { // Изменено на Exception
                            Toast.makeText(LibraryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    etPlaylistName.setError("Playlist name is required");
                }
            });
        }

        dialog.show();
    }

    private void showPlaylistOptionsDialog(Playlist playlist) {
        String[] options = {"Rename", "Delete"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(playlist.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        renamePlaylist(playlist);
                    } else if (which == 1) {
                        deletePlaylist(playlist);
                    }
                })
                .show();
    }

    private void renamePlaylist(Playlist playlist) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_playlist, null);
        TextInputEditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);
        etPlaylistName.setText(playlist.getName());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Rename Playlist")
                .setView(dialogView)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = etPlaylistName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("name", newName);

                        // Используем Map вместо null, так как сервис ожидает данные
                        firebaseService.updateUserData(update, new FirebaseService.OnUpdateListener() {
                            @Override
                            public void onSuccess() {
                                loadPlaylists();
                            }

                            @Override
                            public void onFailure(Exception e) { // Изменено на Exception
                                Toast.makeText(LibraryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlaylist(Playlist playlist) {
        Toast.makeText(this, "Delete functionality not implemented yet", Toast.LENGTH_SHORT).show();
    }
}