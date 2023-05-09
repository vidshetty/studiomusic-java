package com.example.studiomusic.BottomMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studiomusic.AlbumView.AlbumViewActivity;
import com.example.studiomusic.MusicData.Album;
import com.example.studiomusic.MusicData.Track;
import com.example.studiomusic.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Arrays;
import java.util.List;

public class BottomMenu {

    public static class MenuItem {
        private Integer icon_id = null;
        private String menu_name = null;
        private View.OnClickListener clickListener = null;
        private MenuItem(int id, String name, View.OnClickListener clickListener) {
            this.icon_id = id;
            this.menu_name = name;
            this.clickListener = clickListener;
        };
        public Integer getIconId() {
            return icon_id;
        };
        public String getMenuName() {
            return menu_name;
        };
        public View.OnClickListener getOnClickListener() { return this.clickListener; };
    };
    
    public static enum TYPE {
        HOMESCREEN_ALBUMS,
        HOMESCREEN_QUICKPICKS,
        ALBUMVIEW_TRACKS,
        ALBUMVIEW_ALBUM
    };

    private Album album = null;
    private Context context = null;
    private Activity activity = null;
    private Track track = null;
    private BottomSheetDialog dialog = null;
    private Vibrator vibrator = null;
    private TYPE type = null;

    private View.OnClickListener goToHome_album = view -> {
        if (album == null) return;
        vibrator.vibrate(100);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, AlbumViewActivity.class);
            intent.putExtra("album_id", album.getAlbumId());
            context.startActivity(intent);
            dialog.cancel();
        }, 50);
    };

    private View.OnClickListener goToHome_track = view -> {
        if (track == null) return;
        vibrator.vibrate(100);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, AlbumViewActivity.class);
            intent.putExtra("album_id", track.getAlbumId());
            context.startActivity(intent);
            dialog.cancel();
        }, 50);
    };

    private List<MenuItem> album_menu = Arrays.asList(
            new MenuItem(R.drawable.ic_album_icon, "Go To Album", goToHome_album),
            new MenuItem(R.drawable.ic_play_next_icon, "Play Next", null),
            new MenuItem(R.drawable.ic_addtoqueue_icon, "Add To Queue", null),
            new MenuItem(R.drawable.ic_share_icon, "Share", null)
    );

    private List<MenuItem> track_menu = Arrays.asList(
            new MenuItem(R.drawable.ic_album_icon, "Go To Album", goToHome_track),
            new MenuItem(R.drawable.ic_play_next_icon, "Play Next", null),
            new MenuItem(R.drawable.ic_addtoqueue_icon, "Add to Queue", null),
            new MenuItem(R.drawable.ic_share_icon, "Share", null)
    );

    private List<MenuItem> albumview_track_menu = Arrays.asList(
            new MenuItem(R.drawable.ic_play_next_icon, "Play Next", null),
            new MenuItem(R.drawable.ic_addtoqueue_icon, "Add to Queue", null),
            new MenuItem(R.drawable.ic_share_icon, "Share", null)
    );

    public BottomMenu(TYPE type, Context context, Activity activity) {
        this.type = type;
        this.context = context;
        this.activity = activity;
        this.dialog = new BottomSheetDialog(context);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    };

    public void setAlbum(Album album) {
        if (track != null) return;
        this.album = album;
    };

    public void setTrack(Track track) {
        if (album != null) return;
        this.track = track;
    };

    public void show() {

        dialog.setContentView(R.layout.bottomsheet_menu);

        ImageView cover = dialog.findViewById(R.id.track_or_album_art);
        TextView name = dialog.findViewById(R.id.track_title);
        TextView artist_name = dialog.findViewById(R.id.track_artist);

        RecyclerView recyclerView = dialog.findViewById(R.id.menulist);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        if (type == TYPE.HOMESCREEN_ALBUMS) {
            if (album != null) {
                Glide.with(context)
                        .asBitmap()
                        .load(album.getThumbnail())
                        .into(cover);
                name.setText(album.getAlbum());
                artist_name.setText(album.getAlbumArtist());
                BottomMenuAdapter adapter = new BottomMenuAdapter(context, album_menu);
                recyclerView.setAdapter(adapter);
            }
        }
        else if (type == TYPE.HOMESCREEN_QUICKPICKS) {
            if (track != null) {
                Glide.with(context)
                        .asBitmap()
                        .load(track.getThumbnail())
                        .into(cover);
                name.setText(track.getTitle());
                artist_name.setText(track.getArtist());
                BottomMenuAdapter adapter = new BottomMenuAdapter(context, track_menu);
                recyclerView.setAdapter(adapter);
            }
        }
        else if (type == TYPE.ALBUMVIEW_TRACKS) {
            if (track != null) {
                Glide.with(context)
                        .asBitmap()
                        .load(track.getThumbnail())
                        .into(cover);
                name.setText(track.getTitle());
                artist_name.setText(track.getArtist());
                BottomMenuAdapter adapter = new BottomMenuAdapter(context, albumview_track_menu);
                recyclerView.setAdapter(adapter);
            }
        }

        dialog.cancel();
        dialog.show();

    };

};
