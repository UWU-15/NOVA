// DeezerTrackAdapter.java
package com.example.nova.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nova.R;
import com.example.nova.models.DeezerTrack;
import java.util.List;

public class DeezerTrackAdapter extends RecyclerView.Adapter<DeezerTrackAdapter.TrackViewHolder> {

    private List<DeezerTrack> tracks;
    private OnTrackClickListener listener;
    private boolean isDarkTheme = false;

    public interface OnTrackClickListener {
        void onTrackClick(DeezerTrack track, int position);
        void onLikeClick(DeezerTrack track, int position);
    }

    public void setOnTrackClickListener(OnTrackClickListener listener) {
        this.listener = listener;
    }

    public void setTracks(List<DeezerTrack> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    public void setDarkTheme(boolean darkTheme) {
        isDarkTheme = darkTheme;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        DeezerTrack track = tracks.get(position);
        holder.bind(track, position);
    }

    @Override
    public int getItemCount() {
        return tracks == null ? 0 : tracks.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAlbumArt;
        private TextView tvSongTitle, tvSongArtist;
        private ImageButton btnLikeButton;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use correct IDs that exist in your layout
            ivAlbumArt = itemView.findViewById(R.id.ivAlbumArt);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
            btnLikeButton = itemView.findViewById(R.id.btnLikeButton);
        }

        public void bind(DeezerTrack track, int position) {
            tvSongTitle.setText(track.getTitle());
            tvSongArtist.setText(track.getArtistName());

            // Load album art
            if (track.getAlbumCoverUrl() != null && !track.getAlbumCoverUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(track.getAlbumCoverUrl())
                        .placeholder(R.drawable.placeholder_album)
                        .error(R.drawable.placeholder_album)
                        .into(ivAlbumArt);
            } else {
                ivAlbumArt.setImageResource(R.drawable.placeholder_album);
            }

            // Set like button state
            if (track.isLiked()) {
                btnLikeButton.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                btnLikeButton.setImageResource(android.R.drawable.btn_star_big_off);
            }

            // Set theme colors
            if (isDarkTheme) {
                tvSongTitle.setTextColor(0xFFFFFFFF);
                tvSongArtist.setTextColor(0xFFCCCCCC);
            } else {
                tvSongTitle.setTextColor(0xFF000000);
                tvSongArtist.setTextColor(0xFF666666);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackClick(track, position);
                }
            });

            btnLikeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(track, position);
                }
            });
        }
    }
}