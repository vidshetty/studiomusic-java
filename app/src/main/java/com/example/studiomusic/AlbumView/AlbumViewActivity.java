package com.example.studiomusic.AlbumView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.BottomMenu.BottomMenu;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.Main.MainActivity;
import com.example.studiomusic.MusicData.Album;
import com.example.studiomusic.MusicData.Track;
import com.example.studiomusic.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {

    private static ImageView backButton = null;
    private static ImageView loader_backButton = null;
    private static TextView album_name = null;
    private static ImageView album_art = null;
    private static Album album = null;
    private static Vibrator vibrator = null;
    private static ConstraintLayout main = null;
    private static TextView album_artist = null;
    private static TextView album_type = null;
    private static TextView album_year = null;
    private static Toolbar toolbar = null;
    private static NestedScrollView scrollView = null;
    private static ConstraintLayout loader = null;
    private static RecyclerView track_listview = null;
    private static Album_Track_Adapter adapter = null;
    private static TextView total_tracks = null;
    private static TextView total_duration = null;

    private static boolean isToolBarBlack = false;

    private final AlbumTracksTouch albumTracksTouch = new AlbumTracksTouch() {
        @Override
        public void click(int position) {

        };
        @Override
        public void menuClick(int position) {
            vibrator.vibrate(100);
            BottomMenu bottomMenu = new BottomMenu(BottomMenu.TYPE.ALBUMVIEW_TRACKS, AlbumViewActivity.this, AlbumViewActivity.this);
            bottomMenu.setTrack(album.getTracks().get(position));
            bottomMenu.show();
        };
    };

    @Override
    public void onBackPressed() {
        onBackPress(false);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        main = findViewById(R.id.albumview_mainlayout);

        backButton = findViewById(R.id.albumview_backButton);
        backButton.setOnClickListener(view -> onBackPress(true));

        loader_backButton = findViewById(R.id.albumview_backButton_loader);
        loader_backButton.setOnClickListener(view -> onBackPress(true));

        album_name = findViewById(R.id.albumview_name);
        album_art = findViewById(R.id.albumview_albumart);
        album_artist = findViewById(R.id.albumview_albumartist);
        album_type = findViewById(R.id.albumview_type);
        album_year = findViewById(R.id.albumview_year);
        toolbar = findViewById(R.id.albumviewToolbar);
        scrollView = findViewById(R.id.albumview_scrollview);
        loader = findViewById(R.id.albumview_loader);
        total_tracks = findViewById(R.id.totaltracks);
        total_duration = findViewById(R.id.total_duration);
        track_listview = findViewById(R.id.albumview_tracklist);
        track_listview.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        JSONObject query = new JSONObject();
        try {
            String albumId = getIntent().getStringExtra("album_id");
            if (
                albumId == null &&
                getIntent().getData() != null
            ) {
                albumId = getIntent().getData().getPathSegments().get(2);
            }
            query.put("albumId", albumId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIService.getAlbum(getApplicationContext(), query, this::response, this::responseError);

    };

    private void response(JSONObject response) {

        if (response == null) {
            Log.d("servicelog", "response is null");
            return;
        }
        album = Common.buildAlbum(response);
        if (album == null) return;

        Glide.with(this.getApplicationContext())
                .asBitmap()
                .load(album.getThumbnail())
                .into(album_art);

        album_name.setText(album.getAlbum());
        album_artist.setText(album.getAlbumArtist());
        album_type.setText(album.getType());
        album_year.setText(album.getYear());

        this.setBackground();

        List<Track> tracks = album.getTracks();
        adapter = new Album_Track_Adapter(getApplicationContext(), tracks, albumTracksTouch);
        track_listview.setAdapter(adapter);

        total_tracks.setText(new StringBuilder().append(tracks.size()).append(tracks.size() > 1 ? " songs" : " song").toString());
        total_duration.setText(getTotalDuration(tracks));

//        Transition transition = new Fade();
//        transition.setDuration(100);
//        transition.addTarget(R.id.albumview_loader);
//        TransitionManager.beginDelayedTransition(findViewById(R.id.albumview_parent), transition);

        loader.setVisibility(View.GONE);

    };

    private void responseError(VolleyError err) {
        if (APIService.isAuthOrTimeError(err)) {
            APIService.errorHandler(getApplicationContext(), err);
            return;
        }
        Toast.makeText(getApplicationContext(), "Error finding the album!", Toast.LENGTH_SHORT).show();
    };

    private static String convert(String color) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        int nred = (int) ((int) red + (-red * 0.5));
        int ngreen = (int) ((int) green + (-green * 0.5));
        int nblue = (int) ((int) blue + (-blue * 0.5));
        String hex = String.format("#%02x%02x%02x", nred, ngreen, nblue);
        return hex;
//        return "#80" + hex.substring(1);
    };

    private static String getTotalDuration(List<Track> tracks) {

        StringBuilder sb = new StringBuilder();

        int minutes = 0, seconds = 0;

        for (int i=0; i<tracks.size(); i++) {
            String[] duration = tracks.get(i).getDuration().split(": ");
            minutes += Integer.parseInt(duration[0]);
            seconds += Integer.parseInt(duration[1]);
        }

        if (seconds >= 60) {
            minutes += (seconds/60);
            seconds = seconds % 60;
        }

        sb.append(minutes);
        sb.append(" minutes");
        if (seconds > 0) {
            sb.append(" ");
            sb.append(seconds);
            sb.append(" seconds");
        }

        return sb.toString();

    };

    private void setBackground() {

        String hexColor = convert(album.getColor());

        int[] colors = {
                Color.parseColor(hexColor),
                Color.parseColor("#000000")
        };

        GradientDrawable gd_main = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        main.setBackground(gd_main);

        getWindow().setStatusBarColor(Color.parseColor(hexColor));

    };

    private void onBackPress(boolean should_vibrate) {
        if (should_vibrate) vibrator.vibrate(100);
        boolean goHome = isTaskRoot();
        if (goHome) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    };

};