package com.app.studiomusic.FragMoreSearch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.studiomusic.Common.EqualizerView;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.R;

import java.util.List;

public class MoreSearchAlbumsAdapter extends RecyclerView.Adapter<MoreSearchAlbumsAdapter.AlbumsHolder> {

    private Context context = null;
    private List<Album> albums = null;
    private AlbumsTouch albumsTouch = null;

    public MoreSearchAlbumsAdapter(Context context, List<Album> albums, AlbumsTouch albumsTouch) {
        this.context = context;
        this.albums = albums;
        this.albumsTouch = albumsTouch;
    };

    @NonNull
    @Override
    public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_albums, parent, false);
        AlbumsHolder holder = new AlbumsHolder(view);
        Log.d("create_view", "on create");
        return holder;
    };

    @Override
    public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {

        Log.d("bind_view", position + "");

        Album album = albums.get(position);

        holder.equalizerView.setVisibility(View.GONE);
        holder.title.setText(album.getAlbum());
        holder.artist.setText(album.getAlbumArtist());
        holder.type.setText(album.getType());

        Glide.with(context)
                .asBitmap()
                .load(album.getThumbnail())
                .into(holder.albumart);

    };

    @Override
    public int getItemCount() {
        return albums.size();
    };

    public class AlbumsHolder extends RecyclerView.ViewHolder {

        private ImageView albumart = null;
        private EqualizerView equalizerView = null;
        private TextView title = null;
        private TextView artist = null;
        private TextView type = null;
        private RelativeLayout menu = null;

        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            albumart = itemView.findViewById(R.id.search_albums_albumart);
            equalizerView = itemView.findViewById(R.id.search_albums_equalizerview);
            title = itemView.findViewById(R.id.search_albums_title);
            artist = itemView.findViewById(R.id.search_albums_artist);
            type = itemView.findViewById(R.id.search_albums_type);
            menu = itemView.findViewById(R.id.search_albums_menu);
            menu.setOnClickListener(view -> {
                albumsTouch.menuClick(getAdapterPosition());
            });
            itemView.setOnClickListener(view -> {
                albumsTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                return albumsTouch.longClick(getAdapterPosition());
            });
        };

    };

};
