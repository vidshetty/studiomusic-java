package com.app.studiomusic.NowPlayingNew;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.studiomusic.MusicData.QueueTrack;
import com.app.studiomusic.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class NowPlaying_Adapter extends RecyclerView.Adapter<NowPlaying_Adapter.NowPlayingViewHolder> {

    private Context context = null;
    private List<QueueTrack> tracks = null;

    public NowPlaying_Adapter(Context context, List<QueueTrack> tracks) {
        this.context = context;
        this.tracks = tracks;
    };

    @NonNull
    @Override
    public NowPlayingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.nowplaying_albumart, parent, false);
        NowPlayingViewHolder holder = new NowPlayingViewHolder(view);
        return holder;
    };

    @Override
    public void onBindViewHolder(@NonNull NowPlayingViewHolder holder, int position) {

        holder.cardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                holder.cardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = holder.cardView.getHeight();
                int width = holder.cardView.getWidth();

                int min = Math.min(height, width);

                ViewGroup.LayoutParams params = holder.albumart.getLayoutParams();
                params.height = min;
                params.width = min;
                holder.albumart.setLayoutParams(params);

            }
        });

        Glide.with(context)
            .asBitmap()
            .load(tracks.get(position).getThumbnail())
            .into(holder.albumart);

    };

    @Override
    public int getItemCount() {
        return tracks.size();
    };

    public class NowPlayingViewHolder extends RecyclerView.ViewHolder {

        private ShapeableImageView albumart = null;
        private CardView cardView = null;

        public NowPlayingViewHolder(@NonNull View itemView) {
            super(itemView);
            albumart = itemView.findViewById(R.id.nowplaying_thumbnail);
            cardView = itemView.findViewById(R.id.nowplaying_thumbnail_cardview);
        };

    };

};
