package com.app.studiomusic.Lyrics;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LyricsActivity extends AppCompatActivity {

    private static final String tag = "lyrics_log";
    private static final double PARENT_DARKEN_FACTOR = 0.2;
    private static final double NAV_DARKEN_FACTOR = 0.3;
    private static final double DEFAULT_PARENT_FACTOR = 0.3;
    private static final double DEFAULT_NAV_FACTOR = 0.4;

    private ImageView lyrics_down = null;
    private ConstraintLayout lyrics_down_container = null;
    private Toolbar lyrics_toolbar = null;
    private View lyrics_status_bar = null;
    private View lyrics_navigation_bar = null;
    private ConstraintLayout lyrics_navigation = null;
    private ProgressBar lyrics_progressbar = null;
    private ConstraintLayout lyrics_rewind_icon_container = null;
    private ImageView lyrics_rewind_icon = null;
    private ImageView lyrics_forward_icon = null;
    private ConstraintLayout lyrics_forward_icon_container = null;
    private ImageView lyrics_playbutton = null;
    private ImageView lyrics_pausebutton = null;
    private TextView lyrics_synctext = null;
    private ConstraintLayout lyrics_main_loader = null;
    private ConstraintLayout lyrics_parent = null;
    private ConstraintLayout lyrics_content = null;
    private RecyclerView lyrics_recyclerview = null;
    private RecyclerView.SmoothScroller smoothScroller_firstTime = null;
    private RecyclerView.SmoothScroller smoothScroller = null;
    private LinearLayoutManager linearLayoutManager = null;

    private Handler progressHandler = null;
    private Runnable progressRunnable = null;
    private Handler syncHandler = null;
    private Runnable syncRunnable = null;
    private List<SpotifyLyrics> lyrics_list = null;
    private Lyrics_RecyclerView_Adapter adapter = null;

    private boolean is_color_set = false;
    private Track nowplaying_track = null;

    private boolean lyrics_progressbar_max_set = false;
    private boolean initialPlayPauseButtonSet = false;

    private MusicForegroundService.NowPlayingData nowPlayingData_instance = null;
    private ServiceConnection serviceConnection = null;
    private LyricsBroadcastReceiver receiver = null;
    private MusicForegroundService music_service = null;

    private class LyricsBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleLyricsPlayer();
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lyrics);
        overridePendingTransition(R.anim.nowplaying_open, R.anim.previous_stay);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        initialise();
        createView();

    };

    private void initialise() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        MusicForegroundService.NowPlayingData.getInstance(this);

        if (!MusicForegroundService.NowPlayingData.getInstance(this).getTrack().hasLyrics()) {
            closeLyrics();
            return;
        }

        nowplaying_track = MusicForegroundService.NowPlayingData.getInstance(this).getTrack();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (smoothScroller_firstTime == null) {
            smoothScroller_firstTime = new LinearSmoothScroller(this) {
                @Override
                protected int calculateTimeForScrolling(int dx) {
                    return super.calculateTimeForScrolling(dx);
                };
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                };
            };
        }

        if (smoothScroller == null) {
            smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int calculateTimeForScrolling(int dx) {
                    return super.calculateTimeForScrolling(dx) * 5;
                };
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                };
            };
        }

        IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_CHANGE);
        if (receiver == null) receiver = new LyricsBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    music_service = ((MusicForegroundService.MusicForegroundServiceBinder) iBinder).getService();
                };
                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    music_service = null;
                };
            };
        };

        MusicService.bind(this, serviceConnection);

        lyrics_progressbar_max_set = false;
        initialPlayPauseButtonSet = false;

        if (lyrics_progressbar == null) lyrics_progressbar = findViewById(R.id.lyrics_progressbar);
        if (lyrics_playbutton == null) lyrics_playbutton = findViewById(R.id.lyrics_playbutton);
        if (lyrics_pausebutton == null) lyrics_pausebutton = findViewById(R.id.lyrics_pausebutton);

        lyrics_pausebutton.setVisibility(View.VISIBLE);
        lyrics_playbutton.setVisibility(View.GONE);

        runProgressHandler();
        runLyricsSyncHandler();

    };

    private void createView() {

        int status_height = Common.getStatusBarHeight(this);
        int nav_height = Common.getNavigationBarHeight(this);

        if (lyrics_toolbar == null) lyrics_toolbar = findViewById(R.id.lyrics_toolbar);

        if (lyrics_status_bar == null) lyrics_status_bar = findViewById(R.id.lyrics_status_bar);
        ViewGroup.LayoutParams view_params = lyrics_status_bar.getLayoutParams();
        view_params.height = status_height;
        lyrics_status_bar.setLayoutParams(view_params);

        if (lyrics_navigation_bar == null) lyrics_navigation_bar = findViewById(R.id.lyrics_navigation_bar);
        ViewGroup.LayoutParams view_params1 = lyrics_navigation_bar.getLayoutParams();
        view_params1.height = nav_height;
        lyrics_navigation_bar.setLayoutParams(view_params1);

        if (lyrics_navigation == null) lyrics_navigation = findViewById(R.id.lyrics_navigation);

        if (lyrics_parent == null) lyrics_parent = findViewById(R.id.lyrics_parent);

        if (lyrics_content == null) lyrics_content = findViewById(R.id.lyrics_content);

        if (lyrics_main_loader == null) lyrics_main_loader = findViewById(R.id.lyrics_main_loader);
        if (lyrics_main_loader.getVisibility() == View.GONE) {
            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(lyrics_main_loader);
                TransitionManager.beginDelayedTransition(lyrics_parent, transition);
                lyrics_main_loader.setVisibility(View.VISIBLE);
            }, 0);
        }

        if (lyrics_recyclerview == null) lyrics_recyclerview = findViewById(R.id.lyrics_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lyrics_recyclerview.setLayoutManager(linearLayoutManager);

        if (lyrics_down == null) lyrics_down = findViewById(R.id.lyrics_down);
        if (lyrics_down_container == null) lyrics_down_container = findViewById(R.id.lyrics_down_container);
        lyrics_down_container.setOnClickListener(v -> {
            new Handler().postDelayed(this::closeLyrics, 100);
        });

        if (lyrics_synctext == null) lyrics_synctext = findViewById(R.id.lyrics_synctext);
        if (MusicForegroundService.NowPlayingData.getInstance(this).getTrack().hasSync()) lyrics_synctext.setText("Lyrics are time synced");
        else lyrics_synctext.setText("Lyrics are not time synced");

        if (lyrics_playbutton == null) lyrics_playbutton = findViewById(R.id.lyrics_playbutton);
        lyrics_playbutton.setOnClickListener(v -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            if (!music_service.isPlaying()) music_service.play_manual();
            lyrics_playbutton.setVisibility(View.GONE);
            lyrics_pausebutton.setVisibility(View.VISIBLE);
        });

        if (lyrics_pausebutton == null) lyrics_pausebutton = findViewById(R.id.lyrics_pausebutton);
        lyrics_pausebutton.setOnClickListener(v -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.pause_manual();
            lyrics_pausebutton.setVisibility(View.GONE);
            lyrics_playbutton.setVisibility(View.VISIBLE);
        });

        if (lyrics_rewind_icon == null) lyrics_rewind_icon = findViewById(R.id.lyrics_rewind_icon);
        if (lyrics_rewind_icon_container == null) lyrics_rewind_icon_container = findViewById(R.id.lyrics_rewind_icon_container);
        lyrics_rewind_icon_container.setOnClickListener(v -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.rewind();
        });

        if (lyrics_forward_icon == null) lyrics_forward_icon = findViewById(R.id.lyrics_forward_icon);
        if (lyrics_forward_icon_container == null) lyrics_forward_icon_container = findViewById(R.id.lyrics_forward_icon_container);
        lyrics_forward_icon_container.setOnClickListener(v -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.forward();
        });

        JSONObject params = new JSONObject();
        try {
            params.put("track_title", MusicForegroundService.NowPlayingData.getInstance(this).getTrack().getTitle());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        APIService.getLyrics(this, params, this::response, this::responseError);

    };

    private void response(JSONArray response) {
        try {

            lyrics_list = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                int startTime = obj.getInt("startTimeMs");
                String word = obj.getString("words");
                lyrics_list.add(new SpotifyLyrics(startTime, word));
            }

            adapter = new Lyrics_RecyclerView_Adapter(this, lyrics_list);
            lyrics_recyclerview.setAdapter(adapter);

            int activeIndex = MusicForegroundService.NowPlayingData.getInstance(this).getActiveLyricIndex();

            if (activeIndex != -1 && linearLayoutManager != null) {
                scrollToPositionFirstTime(activeIndex);
            }

            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(lyrics_main_loader);
                TransitionManager.beginDelayedTransition(lyrics_parent, transition);
                lyrics_main_loader.setVisibility(View.GONE);
            }, 0);

//            updateColors();

        }
        catch(Exception e) {
            Toast.makeText(this, "Error loading lyrics!", Toast.LENGTH_SHORT).show();
        }
    };

    private void responseError(VolleyError err) {
        Toast.makeText(this, "Error loading lyrics!", Toast.LENGTH_SHORT).show();
        closeLyrics();
    };

    private void updateColors() {

        int prev_parent_color = Color.BLACK;
        int prev_nav_color = Color.BLACK;

        int current_parent_color = -1;
        if (nowplaying_track.getLightColor() != null && nowplaying_track.getDarkColor() != null) {
            current_parent_color = Common.convert(nowplaying_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
        } else {
            current_parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
        }

        int current_nav_color = -1;
        if (nowplaying_track.getLightColor() != null &&  nowplaying_track.getDarkColor() != null) {
            current_nav_color = Common.convert(nowplaying_track.getLightColor(), DEFAULT_NAV_FACTOR);
        } else {
            current_nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);
        }

        int[] prev_parent_colors = {prev_parent_color, prev_parent_color};
        int[] current_parent_colors = {current_parent_color, current_parent_color};
        GradientDrawable prev_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors);
        GradientDrawable current_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors);
        TransitionDrawable parent_trans = new TransitionDrawable(new GradientDrawable[] {prev_parent, current_parent});
        lyrics_status_bar.setBackground(parent_trans);

        int[] prev_parent_colors_1 = {prev_parent_color, prev_parent_color};
        int[] current_parent_colors_1 = {current_parent_color, current_parent_color};
        GradientDrawable prev_parent_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors_1);
        GradientDrawable current_parent_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors_1);
        TransitionDrawable parent_trans_1 = new TransitionDrawable(new GradientDrawable[] {prev_parent_1, current_parent_1});
        lyrics_toolbar.setBackground(parent_trans_1);

        int[] prev_parent_colors_2 = {prev_parent_color, prev_parent_color};
        int[] current_parent_colors_2 = {current_parent_color, current_parent_color};
        GradientDrawable prev_parent_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors_2);
        GradientDrawable current_parent_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors_2);
        TransitionDrawable parent_trans_2 = new TransitionDrawable(new GradientDrawable[] {prev_parent_2, current_parent_2});
        lyrics_content.setBackground(parent_trans_2);

        int[] prev_nav_colors_3 = {prev_nav_color, prev_nav_color};
        int[] current_nav_colors_3 = {current_nav_color, current_nav_color};
        GradientDrawable prev_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_3);
        GradientDrawable current_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_3);
        TransitionDrawable nav_trans_3 = new TransitionDrawable(new GradientDrawable[] {prev_nav_3, current_nav_3});
        lyrics_navigation.setBackground(nav_trans_3);

        int[] prev_nav_colors_4 = {prev_nav_color, prev_nav_color};
        int[] current_nav_colors_4 = {current_nav_color, current_nav_color};
        GradientDrawable prev_nav_4 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_4);
        GradientDrawable current_nav_4 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_4);
        TransitionDrawable nav_trans_4 = new TransitionDrawable(new GradientDrawable[] {prev_nav_4, current_nav_4});
        lyrics_navigation_bar.setBackground(nav_trans_4);

        parent_trans.startTransition(500);
        parent_trans_1.startTransition(500);
        parent_trans_2.startTransition(500);
        nav_trans_3.startTransition(500);
        nav_trans_4.startTransition(500);

    };

    private void resetColor() {

        int prev_parent_color = -1;
        if (nowplaying_track.getLightColor() != null && nowplaying_track.getDarkColor() != null) {
            prev_parent_color = Common.convert(nowplaying_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
        } else {
            prev_parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
        }

        int prev_nav_color = -1;
        if (nowplaying_track.getLightColor() != null && nowplaying_track.getDarkColor() != null) {
            prev_nav_color = Common.convert(nowplaying_track.getLightColor(), DEFAULT_NAV_FACTOR);
        } else {
            prev_nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);
        }

        int current_parent_color = Color.BLACK;
        int current_nav_color = Color.BLACK;

        int[] prev_parent_colors = {prev_parent_color, prev_parent_color};
        int[] current_parent_colors = {current_parent_color, current_parent_color};
        GradientDrawable prev_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors);
        GradientDrawable current_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors);
        TransitionDrawable parent_trans = new TransitionDrawable(new GradientDrawable[] {prev_parent, current_parent});
        lyrics_status_bar.setBackground(parent_trans);

        int[] prev_parent_colors_1 = {prev_parent_color, prev_parent_color};
        int[] current_parent_colors_1 = {current_parent_color, current_parent_color};
        GradientDrawable prev_parent_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors_1);
        GradientDrawable current_parent_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors_1);
        TransitionDrawable parent_trans_1 = new TransitionDrawable(new GradientDrawable[] {prev_parent_1, current_parent_1});
        lyrics_toolbar.setBackground(parent_trans_1);

        int[] prev_parent_colors_2 = {prev_parent_color, prev_parent_color};
        int[] current_parent_colors_2 = {current_parent_color, current_parent_color};
        GradientDrawable prev_parent_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors_2);
        GradientDrawable current_parent_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors_2);
        TransitionDrawable parent_trans_2 = new TransitionDrawable(new GradientDrawable[] {prev_parent_2, current_parent_2});
        lyrics_content.setBackground(parent_trans_2);

        int[] prev_nav_colors_3 = {prev_nav_color, prev_nav_color};
        int[] current_nav_colors_3 = {current_nav_color, current_nav_color};
        GradientDrawable prev_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_3);
        GradientDrawable current_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_3);
        TransitionDrawable nav_trans_3 = new TransitionDrawable(new GradientDrawable[] {prev_nav_3, current_nav_3});
        lyrics_navigation.setBackground(nav_trans_3);

        int[] prev_nav_colors_4 = {prev_nav_color, prev_nav_color};
        int[] current_nav_colors_4 = {current_nav_color, current_nav_color};
        GradientDrawable prev_nav_4 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_4);
        GradientDrawable current_nav_4 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_4);
        TransitionDrawable nav_trans_4 = new TransitionDrawable(new GradientDrawable[] {prev_nav_4, current_nav_4});
        lyrics_navigation_bar.setBackground(nav_trans_4);

        parent_trans.startTransition(500);
        parent_trans_1.startTransition(500);
        parent_trans_2.startTransition(500);
        nav_trans_3.startTransition(500);
        nav_trans_4.startTransition(500);

    };

    private void handleLyricsPlayer() {
        if (lyrics_progressbar == null) return;
        if (lyrics_playbutton == null) return;
        if (lyrics_pausebutton == null) return;
        cleanUp();
        lyrics_progressbar.setMax(0);
        lyrics_progressbar.setProgress(0);
        lyrics_progressbar_max_set = false;
        initialPlayPauseButtonSet = false;
        is_color_set = false;
        lyrics_playbutton.setVisibility(View.GONE);
        lyrics_pausebutton.setVisibility(View.VISIBLE);
//        resetColor();
        initialise();
        createView();
    };

    private void scrollToPosition(int position) {
//        if (smoothScroller == null || linearLayoutManager == null) return;
//        smoothScroller.setTargetPosition(Math.max(position - 3, 0));
//        linearLayoutManager.startSmoothScroll(smoothScroller);

        if (smoothScroller == null || linearLayoutManager == null) return;
        smoothScroller.setTargetPosition(Math.max(position - 3, 0));
        linearLayoutManager.startSmoothScroll(smoothScroller);
    };

    private void scrollToPositionFirstTime(int position) {
        linearLayoutManager.scrollToPosition(Math.max(position - 3, 0));
//        if (smoothScroller_firstTime == null || linearLayoutManager == null) return;
//        smoothScroller_firstTime.setTargetPosition(Math.max(position - 3, 0));
//        linearLayoutManager.startSmoothScroll(smoothScroller_firstTime);
    };

    private void runLyricsSyncHandler() {

        if (!MusicForegroundService.NowPlayingData.getInstance(this).getTrack().hasSync()) return;

        syncHandler = new Handler();

        syncRunnable = new Runnable() {
            @Override
            public void run() {

                if (
                        music_service != null && adapter != null &&
                        lyrics_list != null && lyrics_list.size() > 0
                ) {

                    int currentTime = music_service.getCurrentPosition();
                    int activeIndex = MusicForegroundService.NowPlayingData.getInstance(LyricsActivity.this).getActiveLyricIndex();

                    int right_index = -1;
                    for (int i=0; i<lyrics_list.size(); i++) {
                        if (currentTime < lyrics_list.get(i).getStartTime()) break;
                        right_index = i;
                    }

                    if (right_index != activeIndex) {
                        MusicForegroundService.NowPlayingData.getInstance(LyricsActivity.this).setActiveLyricIndex(right_index);
                        adapter.notifyItemChanged(activeIndex);
                        adapter.notifyItemChanged(right_index);
                        int finalRight_index = right_index;
                        new Handler().postDelayed(() -> {
                            scrollToPosition(finalRight_index);
                        }, 500);
                    }

                }

                syncHandler.postDelayed(this, 100);

            };
        };

        syncHandler.postDelayed(syncRunnable, 100);

    };

    private void runProgressHandler() {

        progressHandler = new Handler();

        progressRunnable = new Runnable() {
            @Override
            public void run() {

                MusicForegroundService.NowPlayingData currentTrack = MusicForegroundService.NowPlayingData.getInstance(getApplicationContext());

                // initial play/pause button set
                if (!initialPlayPauseButtonSet && currentTrack.getMediaPrepared()) {
                    lyrics_playbutton.setVisibility(View.GONE);
                    lyrics_pausebutton.setVisibility(View.VISIBLE);
                    initialPlayPauseButtonSet = true;
                }

                // set duration to seekbar
                if (
                        lyrics_progressbar != null && music_service != null &&
                        !lyrics_progressbar_max_set && currentTrack.getMediaPrepared()
                ) {
                    lyrics_progressbar.setMax(music_service.getDuration());
                    lyrics_progressbar_max_set = true;
                }

                // set current position to seekbar
                if (
                        lyrics_progressbar != null &&
                        music_service != null
                ) {
                    lyrics_progressbar.setProgress(music_service.getCurrentPosition());
                }

                // check which button to show
                if (
                        music_service != null &&
                        music_service.isPlaying() &&
                        lyrics_pausebutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    lyrics_playbutton.setVisibility(View.GONE);
                    lyrics_pausebutton.setVisibility(View.VISIBLE);
                }

                // check which button to show
                if (
                        music_service != null &&
                        !music_service.isPlaying() &&
                        lyrics_playbutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    lyrics_pausebutton.setVisibility(View.GONE);
                    lyrics_playbutton.setVisibility(View.VISIBLE);
                }

                progressHandler.postDelayed(this, 100);

            };
        };

        progressHandler.postDelayed(progressRunnable, 100);

    };

    private void cleanUp() {
        if (progressHandler != null && progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressHandler = null;
            progressRunnable = null;
        }
        if (syncHandler != null && syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
            syncHandler = null;
            syncRunnable = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    };

    @Override
    public void onBackPressed() {
        closeLyrics();
        super.onBackPressed();
    };

    private void closeLyrics() {
        cleanUp();
        finish();
        overridePendingTransition(R.anim.previous_stay, R.anim.nowplaying_close);
    };

    @Override
    protected void onDestroy() {
        cleanUp();
        super.onDestroy();
    };

};