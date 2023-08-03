package com.example.studiomusic.Lyrics;

import com.android.volley.VolleyError;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.Audio_Controller.MusicApplication;
import com.example.studiomusic.Audio_Controller.MusicForegroundService;
import com.example.studiomusic.Audio_Controller.MusicForegroundService.NowPlayingData;
import com.example.studiomusic.Audio_Controller.MusicServiceNew;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LyricsActivity extends AppCompatActivity {

    private static final String tag = "lyrics_log";

    private ImageView lyrics_down = null;
    private ProgressBar lyrics_progressbar = null;
    private ImageView lyrics_rewind_icon = null;
    private ImageView lyrics_forward_icon = null;
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

    private boolean lyrics_progressbar_max_set = false;
    private boolean initialPlayPauseButtonSet = false;

    private NowPlayingData nowPlayingData_instance = null;
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

        initialise();
        createView();

    };

    private void initialise() {

        NowPlayingData.getInstance(this);

        if (!NowPlayingData.getInstance(this).getTrack().hasLyrics()) {
            closeLyrics();
            return;
        }

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

        MusicServiceNew.bind(this, serviceConnection);

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
        lyrics_down.setOnClickListener(v -> {
            closeLyrics();
        });

        if (lyrics_synctext == null) lyrics_synctext = findViewById(R.id.lyrics_synctext);
        if (NowPlayingData.getInstance(this).getTrack().hasSync()) lyrics_synctext.setText("Lyrics are time synced");
        else lyrics_synctext.setText("Lyrics are not time synced");

        if (lyrics_playbutton == null) lyrics_playbutton = findViewById(R.id.lyrics_playbutton);
        lyrics_playbutton.setOnClickListener(v -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            if (!music_service.isPlaying()) music_service.play_manual();
            lyrics_playbutton.setVisibility(View.GONE);
            lyrics_pausebutton.setVisibility(View.VISIBLE);
        });

        if (lyrics_pausebutton == null) lyrics_pausebutton = findViewById(R.id.lyrics_pausebutton);
        lyrics_pausebutton.setOnClickListener(v -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            music_service.pause_manual();
            lyrics_pausebutton.setVisibility(View.GONE);
            lyrics_playbutton.setVisibility(View.VISIBLE);
        });

        if (lyrics_rewind_icon == null) lyrics_rewind_icon = findViewById(R.id.lyrics_rewind_icon);
        lyrics_rewind_icon.setOnClickListener(view -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            music_service.rewind();
        });

        if (lyrics_forward_icon == null) lyrics_forward_icon = findViewById(R.id.lyrics_forward_icon);
        lyrics_forward_icon.setOnClickListener(view -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            music_service.forward();
        });

        JSONObject params = new JSONObject();
        try {
            params.put("track_title", NowPlayingData.getInstance(this).getTrack().getTitle());
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

            int activeIndex = NowPlayingData.getInstance(this).getActiveLyricIndex();

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

        }
        catch(Exception e) {
            Toast.makeText(this, "Error loading lyrics!", Toast.LENGTH_SHORT).show();
        }
    };

    private void responseError(VolleyError err) {
        Toast.makeText(this, "Error loading lyrics!", Toast.LENGTH_SHORT).show();
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
        lyrics_playbutton.setVisibility(View.GONE);
        lyrics_pausebutton.setVisibility(View.VISIBLE);
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
        if (smoothScroller_firstTime == null || linearLayoutManager == null) return;
        smoothScroller_firstTime.setTargetPosition(Math.max(position - 3, 0));
        linearLayoutManager.startSmoothScroll(smoothScroller_firstTime);
    };

    private void runLyricsSyncHandler() {

        if (!NowPlayingData.getInstance(this).getTrack().hasSync()) return;

        syncHandler = new Handler();

        syncRunnable = new Runnable() {
            @Override
            public void run() {

                if (
                        music_service != null && adapter != null &&
                        lyrics_list != null && lyrics_list.size() > 0
                ) {

                    int currentTime = music_service.getCurrentPosition();
                    int activeIndex = NowPlayingData.getInstance(getApplicationContext()).getActiveLyricIndex();

                    int right_index = -1;
                    for (int i=0; i<lyrics_list.size(); i++) {
                        if (currentTime < lyrics_list.get(i).getStartTime()) break;
                        right_index = i;
                    }

                    if (right_index != activeIndex) {
                        NowPlayingData.getInstance(getApplicationContext()).setActiveLyricIndex(right_index);
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

                NowPlayingData currentTrack = NowPlayingData.getInstance(getApplicationContext());

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