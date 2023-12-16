package com.app.studiomusic.FragAlbumView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Common.EqualizerView;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import java.util.List;

public class Album_Track_Adapter extends RecyclerView.Adapter<Album_Track_Adapter.TrackHolder> {

    private Context context = null;
    private List<Track> albumList = null;
    private AlbumTracksTouch albumTracksTouch = null;

    public Album_Track_Adapter(Context context, List<Track> albumList, AlbumTracksTouch albumTracksTouch) {
        this.context = context;
        this.albumList = albumList;
        this.albumTracksTouch = albumTracksTouch;
    };

    @NonNull
    @Override
    public TrackHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.albumview_track, parent, false);
        TrackHolder viewHolder = new TrackHolder(view);
        return viewHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull TrackHolder holder, int position) {

        Track current = albumList.get(position);
        int trackNo = position + 1;
        holder.track_title.setText(current.getTitle());
        holder.track_artist.setText(current.getArtist());
        holder.track_duration.setText(current.getDuration());

        Track now_playing = MusicForegroundService.NowPlayingData.getInstance(context).getTrack();

        if (current.hasLyrics()) holder.lyrics_tag.setVisibility(View.VISIBLE);
        else holder.lyrics_tag.setVisibility(View.GONE);

        if (now_playing == null || !now_playing.getTrackId().equals(current.getTrackId())) {
            holder.track_number.setVisibility(View.VISIBLE);
            holder.equalizerView.setVisibility(View.GONE);
            holder.track_number.setText(String.valueOf(trackNo));
            return;
        }

        holder.track_number.setVisibility(View.GONE);
        holder.equalizerView.setVisibility(View.VISIBLE);

        if (MusicForegroundService.NowPlayingData.getInstance(context).getMediaIsPlaying()) {
            holder.equalizerView.animateBars();
        } else {
            holder.equalizerView.stopBars();
        }

    };

    @Override
    public int getItemCount() {
        return albumList.size();
    };

    public class TrackHolder extends RecyclerView.ViewHolder {

        private TextView track_number = null;
        private TextView track_title = null;
        private TextView track_artist = null;
        private TextView track_duration = null;
        private RelativeLayout track_menu = null;
        private EqualizerView equalizerView = null;
        private LinearLayout lyrics_tag = null;

        public TrackHolder(@NonNull View itemView) {
            super(itemView);
            track_number = itemView.findViewById(R.id.track_number);
            equalizerView = itemView.findViewById(R.id.albumview_equalizerview);
            track_title = itemView.findViewById(R.id.track_title);
            track_artist = itemView.findViewById(R.id.track_artist);
            track_duration = itemView.findViewById(R.id.track_duration);
            track_menu = itemView.findViewById(R.id.track_menu);
            lyrics_tag = itemView.findViewById(R.id.track_lyrics_tag);
            itemView.setOnClickListener(view -> {
                albumTracksTouch.trackClick(getAdapterPosition());
            });
            track_menu.setOnClickListener(view -> {
                albumTracksTouch.trackMenuClick(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                albumTracksTouch.trackMenuClick(getAdapterPosition());
                return true;
            });
        };

    };

}
