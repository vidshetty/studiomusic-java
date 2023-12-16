package com.app.studiomusic.FragLibrary;

import android.content.Context;
import android.util.Log;
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

public class Library_RecyclerView_Adapter extends RecyclerView.Adapter<Library_RecyclerView_Adapter.AlbumViewHolder> {

    private Context context = null;
    private List<Album> albumList = null;
    private LibraryTouch libraryTouch = null;

    public Library_RecyclerView_Adapter(Context context, List<Album> albumList, LibraryTouch libraryTouch) {
        this.context = context;
        this.albumList = albumList;
        this.libraryTouch = libraryTouch;
    };

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.library_albumview, parent, false);
        AlbumViewHolder viewHolder = new AlbumViewHolder(view);
        return viewHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Log.d("servicelog", "position " + position);
        Album current = albumList.get(position);
        holder.album_art.setMinimumHeight(holder.album_art.getWidth());
        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(current.getThumbnail())
                .into(holder.album_art);
        holder.album_title.setText(current.getAlbum());
        holder.album_artist.setText(current.getAlbumArtist());
    };

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {

        public ImageView album_art = null;
        public TextView album_title = null;
        public TextView album_artist = null;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            album_art = itemView.findViewById(R.id.library_albumart);
            album_title = itemView.findViewById(R.id.library_title);
            album_artist = itemView.findViewById(R.id.library_albumartist);
            itemView.setOnClickListener(view -> {
                libraryTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                return libraryTouch.longClick(getAdapterPosition());
            });
        }

    }

}
