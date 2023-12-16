package com.app.studiomusic.FragHomescreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.R;

import java.util.List;

public class RecentlyAdded_Adapter extends RecyclerView.Adapter<RecentlyAdded_Adapter.RecentlyAddedHolder> {

    private Context context = null;
    private List<Album> recentlyAdded = null;
    private RecentlyAddedTouch recentlyAddedTouch = null;

    public RecentlyAdded_Adapter(Context context, List<Album> recentlyAdded, RecentlyAddedTouch recentlyAddedTouch) {
        this.context = context;
        this.recentlyAdded = recentlyAdded;
        this.recentlyAddedTouch = recentlyAddedTouch;
    };

    @NonNull
    @Override
    public RecentlyAddedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.homescreen_other_albums, parent, false);
        RecentlyAddedHolder holder = new RecentlyAddedHolder(view);
        return holder;
    };

    @Override
    public void onBindViewHolder(@NonNull RecentlyAddedHolder holder, int position) {
        Album album = recentlyAdded.get(position);
        Glide.with(context)
                .asBitmap()
                .load(album.getThumbnail())
                .into(holder.imageView);
        holder.title.setText(album.getAlbum());
        holder.albumArtist.setText(album.getAlbumArtist());
    };

    @Override
    public int getItemCount() {
        return recentlyAdded.size();
    }

    public class RecentlyAddedHolder extends RecyclerView.ViewHolder {

        private ImageView imageView = null;
        private TextView title = null;
        private TextView albumArtist = null;

        public RecentlyAddedHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.mostplayed_albumart);
            title = itemView.findViewById(R.id.mostplayed_title);
            albumArtist = itemView.findViewById(R.id.mostplayed_albumartist);
            itemView.setOnClickListener(view -> {
                recentlyAddedTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                return recentlyAddedTouch.longClick(getAdapterPosition());
            });
        };

    };

};
