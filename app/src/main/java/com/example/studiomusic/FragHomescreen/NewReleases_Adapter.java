package com.example.studiomusic.FragHomescreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studiomusic.MusicData.Album;
import com.example.studiomusic.R;

import java.util.List;

public class NewReleases_Adapter extends RecyclerView.Adapter<NewReleases_Adapter.NewReleasesHolder> {

    private Context context = null;
    private List<Album> new_releases = null;
    private NewReleasesTouch newReleasesTouch = null;

    public NewReleases_Adapter(Context context, List<Album> new_releases, NewReleasesTouch newReleasesTouch) {
        this.context = context;
        this.new_releases = new_releases;
        this.newReleasesTouch = newReleasesTouch;
    };

    @NonNull
    @Override
    public NewReleasesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.homescreen_other_albums, parent, false);
        NewReleasesHolder holder = new NewReleasesHolder(view);
        return holder;
    };

    @Override
    public void onBindViewHolder(@NonNull NewReleasesHolder holder, int position) {
        Album album = new_releases.get(position);
        Glide.with(context)
                .asBitmap()
                .load(album.getThumbnail())
                .into(holder.imageView);
        holder.title.setText(album.getAlbum());
        holder.albumArtist.setText(album.getAlbumArtist());
    };

    @Override
    public int getItemCount() {
        return new_releases.size();
    }

    public class NewReleasesHolder extends RecyclerView.ViewHolder {

        private ImageView imageView = null;
        private TextView title = null;
        private TextView albumArtist = null;

        public NewReleasesHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.mostplayed_albumart);
            title = itemView.findViewById(R.id.mostplayed_title);
            albumArtist = itemView.findViewById(R.id.mostplayed_albumartist);
            itemView.setOnClickListener(view -> {
                newReleasesTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(view -> {
                return newReleasesTouch.longClick(getAdapterPosition());
            });
        };

    };

};
