// adapters/PlaylistAdapter.java
package com.example.nova.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nova.R;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlists = new ArrayList<>();
    private OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
        void onPlaylistOptionsClick(Playlist playlist, View view);
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {
        this.listener = listener;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        notifyItemInserted(playlists.size() - 1);
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track_main, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist, listener);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        CardView trackPlayCard;
        TextView tvTrackTitle, tvTrackArtist, tvTrackDuration;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            trackPlayCard = itemView.findViewById(R.id.trackPlayCard);
            tvTrackTitle = itemView.findViewById(R.id.tvTrackTitle);
            tvTrackArtist = itemView.findViewById(R.id.tvTrackArtist);
            tvTrackDuration = itemView.findViewById(R.id.tvTrackDuration);
        }

        public void bind(Playlist playlist, OnPlaylistClickListener listener) {
            tvTrackTitle.setText(playlist.getName());
            tvTrackArtist.setText(playlist.getTrackCount() + " songs");
            tvTrackDuration.setText("");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPlaylistClick(playlist);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onPlaylistOptionsClick(playlist, v);
                return true;
            });
        }
    }
}