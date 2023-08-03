package com.example.studiomusic.Lyrics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studiomusic.Audio_Controller.MusicForegroundService;
import com.example.studiomusic.R;

import java.util.List;

public class Lyrics_RecyclerView_Adapter extends RecyclerView.Adapter<Lyrics_RecyclerView_Adapter.LyricViewHolder> {

    private Context context = null;
    private List<SpotifyLyrics> lyricsList = null;

    public Lyrics_RecyclerView_Adapter(Context context, List<SpotifyLyrics> lyricsList) {
        this.context = context;
        this.lyricsList = lyricsList;
    };

    @NonNull
    @Override
    public LyricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lyrics, parent, false);
        LyricViewHolder viewHolder = new LyricViewHolder(view);
        return viewHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull LyricViewHolder holder, int position) {
        holder.text.setText(lyricsList.get(position).getWord());
        int activeIndex = MusicForegroundService.NowPlayingData.getInstance(context).getActiveLyricIndex();
        if (position == activeIndex) holder.text.setTextColor(context.getColor(R.color.white));
        else holder.text.setTextColor(context.getColor(R.color.lyrics_default));
    };

    @Override
    public int getItemCount() {
        return lyricsList.size();
    };

    public class LyricViewHolder extends RecyclerView.ViewHolder {

        private TextView text = null;

        public LyricViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.lyric_text);
        };

    };

};
