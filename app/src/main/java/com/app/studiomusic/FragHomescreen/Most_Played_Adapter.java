package com.app.studiomusic.FragHomescreen;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.studiomusic.Common.EqualizerView;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.R;

import java.util.List;

public class Most_Played_Adapter extends RecyclerView.Adapter<Most_Played_Adapter.MostPlayedHolder> {

    private Context context = null;
    private List<MostPlayedItem> most_played = null;
    private MostPlayedTouch mostPlayedTouch = null;

    public Most_Played_Adapter(Context context, List<MostPlayedItem> most_played, MostPlayedTouch mostPlayedTouch) {
        this.context = context;
        this.most_played = most_played;
        this.mostPlayedTouch = mostPlayedTouch;
    };

    @NonNull
    @Override
    public MostPlayedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.homescreen_most_played, parent, false);
        MostPlayedHolder mostPlayedHolder = new MostPlayedHolder(view);
        return mostPlayedHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull MostPlayedHolder holder, int position) {

        Album album = most_played.get(position).getAlbum();
        Glide.with(context)
                .asBitmap()
                .load(album.getThumbnail())
                .into(holder.imageView);
        holder.title.setText(album.getAlbum());
        holder.albumArtist.setText(album.getAlbumArtist());

        if (!most_played.get(position).getIsNowPlaying()) {
            holder.playbutton.setVisibility(View.VISIBLE);
            holder.equalizerView.setVisibility(View.GONE);
            holder.playbutton_container.setBackgroundColor(Color.argb(1,0,0,0));
            return;
        }

        holder.equalizerView.setVisibility(View.VISIBLE);
        holder.playbutton.setVisibility(View.GONE);
        holder.playbutton_container.setBackgroundColor(Color.argb(0.5f,0f,0f,0f));

        if (most_played.get(position).getIsPaused()) {
            holder.equalizerView.stopBars();
        }
        else {
            if (holder.equalizerView.isAnimating()) holder.equalizerView.stopBars();
            holder.equalizerView.animateBars();
        }

    };

    @Override
    public int getItemCount() { return most_played.size(); }

    public class MostPlayedHolder extends RecyclerView.ViewHolder {

        private ImageView imageView = null;
        private TextView title = null;
        private TextView albumArtist = null;
        private ImageView playbutton = null;
        private EqualizerView equalizerView = null;
        private ConstraintLayout playbutton_container = null;

        public MostPlayedHolder(@NonNull View itemView) {

            super(itemView);
            imageView = itemView.findViewById(R.id.mostplayed_albumart);
            title = itemView.findViewById(R.id.mostplayed_title);
            albumArtist = itemView.findViewById(R.id.mostplayed_albumartist);
            playbutton = itemView.findViewById(R.id.albumart_playbutton);
            playbutton.setOnClickListener(view -> {
                mostPlayedTouch.click(getAdapterPosition());
            });
            playbutton.setOnLongClickListener(view -> {
                return mostPlayedTouch.longClick(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                return mostPlayedTouch.longClick(getAdapterPosition());
            });

            equalizerView = itemView.findViewById(R.id.most_played_equalizerview);

            playbutton_container = itemView.findViewById(R.id.albumart_playbutton_container);

        };

    };

};
