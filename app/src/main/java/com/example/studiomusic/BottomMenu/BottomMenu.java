package com.example.studiomusic.BottomMenu;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studiomusic.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class BottomMenu {

    public static class MenuItem {

        private Integer icon_id = null;
        private String menu_name = null;
        private View.OnClickListener clickListener = null;

        public MenuItem(Integer id, String name, View.OnClickListener clickListener) {
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

    public static class Builder {

        private Context context = null;
        private List<MenuItem> list = null;
        private String thumbnail = null;
        private String name = null;
        private String artist = null;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        };

        public Builder setDataList(List<MenuItem> list) {
            this.list = list;
            return this;
        };

        public Builder setThumbnail(String name) {
            this.thumbnail = name;
            return this;
        };

        public Builder setName(String name) {
            this.name = name;
            return this;
        };

        public Builder setArtist(String name) {
            this.artist = name;
            return this;
        };

        public BottomMenu build() {
            return new BottomMenu(this);
        };

    };

    private BottomSheetDialog dialog = null;
    private Context context = null;
    private List<MenuItem> list = null;

    private String thumbnail = null;
    private String name = null;
    private String artist = null;

    private ImageView cover_imageview = null;
    private TextView name_textview = null;
    private TextView artist_name_textview = null;
    private RecyclerView recyclerView = null;

    private BottomMenu(Builder builder) {

        this.context = builder.context;
        this.dialog = new BottomSheetDialog(context);
        this.list = builder.list;
        this.thumbnail = builder.thumbnail;
        this.name = builder.name;
        this.artist = builder.artist;

        dialog.setContentView(R.layout.bottomsheet_menu);
        dialog.getWindow().setNavigationBarColor(context.getColor(R.color.light_black));

        cover_imageview = dialog.findViewById(R.id.track_or_album_art);
        name_textview = dialog.findViewById(R.id.track_title);
        artist_name_textview = dialog.findViewById(R.id.track_artist);
        recyclerView = dialog.findViewById(R.id.menulist);

    };

    BottomMenuTouch bottomMenuTouch = new BottomMenuTouch() {
        @Override
        public void click(int position) {
            MenuItem item = list.get(position);
            new Handler().postDelayed(() -> {
                if (item.getOnClickListener() != null) {
                    item.getOnClickListener().onClick(null);
                }
                dialog.cancel();
            }, 100);
        };
    };

    public void show() {

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Glide.with(context)
                .asBitmap()
                .load(this.thumbnail)
                .into(cover_imageview);

        name_textview.setText(this.name);
        artist_name_textview.setText(this.artist);
        BottomMenuAdapter adapter = new BottomMenuAdapter(this.context, this.list, bottomMenuTouch);
        recyclerView.setAdapter(adapter);

        dialog.cancel();
        dialog.show();

    };

};
