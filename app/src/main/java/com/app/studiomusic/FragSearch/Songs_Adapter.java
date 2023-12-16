package com.app.studiomusic.FragSearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.studiomusic.Common.EqualizerView;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import java.util.List;

public class Songs_Adapter extends RecyclerView.Adapter<Songs_Adapter.SongsHolder> {

    private Context context = null;
    private List<Track> tracks = null;
    private SongsTouch songsTouch = null;

    public Songs_Adapter(Context context, List<Track> tracks, SongsTouch songsTouch) {
        this.context = context;
        this.tracks = tracks;
        this.songsTouch = songsTouch;
    };

    @NonNull
    @Override
    public SongsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_songs, parent, false);
        SongsHolder holder = new SongsHolder(view);
        return holder;
    };

    @Override
    public void onBindViewHolder(@NonNull SongsHolder holder, int position) {

        Track track = tracks.get(position);

        holder.equalizerView.setVisibility(View.GONE);
        holder.track_title.setText(track.getTitle());
        holder.track_artist.setText(track.getArtist());
        holder.track_duration.setText(track.getDuration());

        Glide.with(context)
            .asBitmap()
            .load(track.getThumbnail())
            .into(holder.albumart);

        if (track.hasLyrics()) holder.lyrics_tag.setVisibility(View.VISIBLE);
        else holder.lyrics_tag.setVisibility(View.GONE);

    };

    @Override
    public int getItemCount() {
        return tracks.size();
    };

    public class SongsHolder extends RecyclerView.ViewHolder {

        private ImageView albumart = null;
        private EqualizerView equalizerView = null;
        private TextView track_title = null;
        private TextView track_artist = null;
        private TextView track_duration = null;
        private RelativeLayout menu = null;
        private LinearLayout lyrics_tag = null;

        public SongsHolder(@NonNull View itemView) {
            super(itemView);
            albumart = itemView.findViewById(R.id.search_songs_albumart);
            equalizerView = itemView.findViewById(R.id.search_songs_equalizerview);
            track_title = itemView.findViewById(R.id.search_songs_track_title);
            track_artist = itemView.findViewById(R.id.search_songs_track_artist);
            track_duration = itemView.findViewById(R.id.search_songs_track_duration);
            menu = itemView.findViewById(R.id.search_songs_track_menu);
            lyrics_tag = itemView.findViewById(R.id.search_songs_lyrics_tag);
            menu.setOnClickListener(view -> {
                songsTouch.menuClick(getAdapterPosition());
            });
            itemView.setOnClickListener(view -> {
                songsTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                return songsTouch.longClick(getAdapterPosition());
            });
        };

    };

};
