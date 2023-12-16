package com.app.studiomusic.FragHomescreen;

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
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import java.util.List;

public class Quick_Picks_Adapter extends RecyclerView.Adapter<Quick_Picks_Adapter.QuickPicksHolder> {

    private Context context = null;
    private List<Track> quickPicks = null;
    private QuickPicksTouch quickPicksTouch = null;

    public Quick_Picks_Adapter(Context context, List<Track> quickPicks, QuickPicksTouch quickPicksTouch) {
        this.context = context;
        this.quickPicks = quickPicks;
        this.quickPicksTouch = quickPicksTouch;
    };

    @NonNull
    @Override
    public QuickPicksHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.homescreen_quick_picks, parent, false);
        QuickPicksHolder quickPicksHolder = new QuickPicksHolder(view);
        return quickPicksHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull QuickPicksHolder holder, int position) {
        Track track = quickPicks.get(position);
        Glide.with(context)
                .asBitmap()
                .load(track.getThumbnail())
                .into(holder.imageView);
        holder.title.setText(track.getTitle());
        holder.artist.setText(track.getArtist());
        if (track.hasLyrics()) holder.lyrics_tag.setVisibility(View.VISIBLE);
        else holder.lyrics_tag.setVisibility(View.GONE);
    };

    @Override
    public int getItemCount() {
        return quickPicks.size();
    }

    public class QuickPicksHolder extends RecyclerView.ViewHolder {

        private ImageView imageView = null;
        private TextView title = null;
        private TextView artist = null;
        private RelativeLayout menu = null;
        private LinearLayout lyrics_tag = null;

        public QuickPicksHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.quickpicks_albumart);
            title = itemView.findViewById(R.id.quickpicks_albumtitle);
            artist = itemView.findViewById(R.id.quickpicks_artist);
            menu = itemView.findViewById(R.id.quickpicks_menu);
            lyrics_tag = itemView.findViewById(R.id.quickpicks_lyrics_tag);
            menu.setOnClickListener(view -> {
                quickPicksTouch.menuClick(getAdapterPosition(), true);
            });
            itemView.setOnClickListener(view -> {
                quickPicksTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                quickPicksTouch.menuClick(getAdapterPosition(), false);
                return true;
            });
        };

    };

};
