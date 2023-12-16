package com.app.studiomusic.NowPlaying;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.bumptech.glide.Glide;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Lyrics.LyricsActivity;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.Queue.QueueActivity;
import com.app.studiomusic.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Arrays;
import java.util.List;

public class NowPlaying extends AppCompatActivity {

    private static final double DARKEN_FACTOR = 0.6;
    private static final double SUB_DARKEN_FACTOR = 1;
    private static final double LIGHTEN_FACTOR = 1.3;
    private static final String tag = "nowplaying_tag";

    private Track nowplaying_track = null;

    private ConstraintLayout nowplaying_thumbnail_holder = null;
    private ConstraintLayout nowplaying_parent = null;
    private ConstraintLayout nowplaying_navigation = null;
//    private ConstraintLayout nowplaying_topcover = null;
    private ImageView nowplaying_down = null;
    private ImageView nowplaying_menu = null;
    private ImageView nowplaying_playbutton = null;
    private ImageView nowplaying_pausebutton = null;
    private ProgressBar nowplaying_progressbar = null;
    private ShapeableImageView nowplaying_thumbnail = null;
    private TextView nowplaying_trackname = null;
    private TextView nowplaying_trackartist = null;
    private TextView nowplaying_lyrics = null;
    private TextView nowplaying_upnext = null;
    private SeekBar nowplaying_seekbar = null;
    private TextView nowplaying_elapsedtime = null;
    private TextView nowplaying_duration = null;
    private ImageView nowplaying_previous_icon = null;
    private ImageView nowplaying_next_icon = null;
//    private TextView nowplaying_fromtext = null;
//    private TextView nowplaying_yeartext = null;
    private TextView nowplaying_type = null;
    private TextView nowplaying_year = null;
    private LinearLayout nowplaying_toolbar_data = null;
    private ImageView nowplaying_repeat = null;
    private ConstraintLayout nowplaying_repeat_holder = null;
    private ImageView nowplaying_shuffle = null;
    private ConstraintLayout nowplaying_shuffle_holder = null;

    private boolean nowplaying_seekbar_max_set = false;
    private boolean isSeekbarOnFocus = false;
    private boolean initialPlayPauseButtonSet = false;

    private Handler progressHandler = null;
    private MusicForegroundService music_service = null;
    private Runnable progressHandlerRunnable = null;
    private CurrentTrackReceiver receiver = null;
    private ServiceConnection serviceConnection = null;
    private ActivityResultLauncher<Intent> launcher = null;

    private class CurrentTrackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            cleanup();
            initialize();
            createView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_now_playing);
        overridePendingTransition(R.anim.nowplaying_open, R.anim.previous_stay);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        initialize();
        createView();

    };

    private int getStatusBarHeight() {
        int height;
        Resources myResources = getResources();
        int idStatusBarHeight = myResources.getIdentifier( "status_bar_height", "dimen", "android");
        if (idStatusBarHeight > 0) {
            height = getResources().getDimensionPixelSize(idStatusBarHeight);
//            Toast.makeText(this, "Status Bar Height = " + height, Toast.LENGTH_LONG).show();
        } else {
            height = 0;
//            Toast.makeText(this, "Resources NOT found", Toast.LENGTH_LONG).show();
        }
        return height;
    }

    @Override
    protected void onDestroy() {
        cleanup();
        super.onDestroy();
    };

    private void initialize() {

        IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_CHANGE);
        if (receiver == null) receiver = new CurrentTrackReceiver();
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
        }

        MusicService.bind(this, serviceConnection);

        if (launcher == null) {
            launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result == null) return;
                        if (result.getResultCode() == 1) {
                            if (result.getData() == null) return;
                            String albumId = result.getData().getStringExtra("album_id");
                            if (albumId == null) return;
                            Intent new_intent = new Intent();
                            new_intent.putExtra("album_id", albumId);
                            closeNowPlaying(new_intent);
                            return;
                        }
                        if (result.getResultCode() == 2) {
                            closeNowPlaying(null);
                        }
                    };
                }
            );
        }

        isSeekbarOnFocus = false;
        nowplaying_seekbar_max_set = false;
        initialPlayPauseButtonSet = false;

        if (nowplaying_progressbar != null) nowplaying_progressbar.setVisibility(View.GONE);
        if (nowplaying_playbutton != null) nowplaying_playbutton.setVisibility(View.GONE);
        if (nowplaying_pausebutton != null) nowplaying_pausebutton.setVisibility(View.GONE);

        runProgressHandler();

    };

    private void createView() {

        nowplaying_track = MusicForegroundService.NowPlayingData.getInstance(this).getTrack();

        String hexColor = convert(darken(nowplaying_track.getColor()));

        getWindow().setStatusBarColor(Color.parseColor(hexColor));

//        if (nowplaying_topcover == null) {
//            nowplaying_topcover = findViewById(R.id.nowplaying_topcover);
//            nowplaying_topcover.setBackgroundColor(Color.parseColor(hexColor));
//            new Handler().postDelayed(() -> {
//                Transition transition = new Fade();
//                transition.setDuration(300);
//                transition.addTarget(nowplaying_topcover);
//                TransitionManager.beginDelayedTransition(nowplaying_parent, transition);
//                nowplaying_topcover.setVisibility(View.GONE);
//            }, 200);
//        }

        if (nowplaying_parent == null) nowplaying_parent = findViewById(R.id.nowplaying_parent);
        nowplaying_parent.setBackgroundColor(Color.parseColor(hexColor));
        int height = getStatusBarHeight();
        nowplaying_parent.setPadding(0, height, 0, 0);

        if (nowplaying_thumbnail == null) nowplaying_thumbnail = findViewById(R.id.nowplaying_thumbnail);
        Glide.with(this).asBitmap().load(nowplaying_track.getThumbnail()).into(nowplaying_thumbnail);

        if (nowplaying_playbutton == null) nowplaying_playbutton = findViewById(R.id.nowplaying_playbutton);
        nowplaying_playbutton.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            if (!music_service.isPlaying()) music_service.play_manual();
            nowplaying_playbutton.setVisibility(View.GONE);
            nowplaying_pausebutton.setVisibility(View.VISIBLE);
        });

        if (nowplaying_pausebutton == null) nowplaying_pausebutton = findViewById(R.id.nowplaying_pausebutton);
        nowplaying_pausebutton.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
//            if (music_service.isPlaying()) music_service.pause_manual();
//            if (!NowPlayingData.getInstance(this).getMediaPrepared()) return;
            music_service.pause_manual();
            nowplaying_pausebutton.setVisibility(View.GONE);
            nowplaying_playbutton.setVisibility(View.VISIBLE);
        });

        if (nowplaying_progressbar == null) nowplaying_progressbar = findViewById(R.id.nowplaying_progressbar);
        if (!MusicForegroundService.NowPlayingData.getInstance(this).getMediaPrepared()) {
//            nowplaying_progressbar.setVisibility(View.VISIBLE);
            nowplaying_progressbar.setVisibility(View.GONE);
            nowplaying_pausebutton.setVisibility(View.VISIBLE);
            Log.d(tag, "setting progress bar");
        }
        else nowplaying_progressbar.setVisibility(View.GONE);

        if (nowplaying_previous_icon == null) nowplaying_previous_icon = findViewById(R.id.nowplaying_previous_icon);
        nowplaying_previous_icon.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.playPreviousTrack();
        });

        if (nowplaying_next_icon == null) nowplaying_next_icon = findViewById(R.id.nowplaying_next_icon);
        nowplaying_next_icon.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.playNextTrack();
        });

        if (nowplaying_down == null) nowplaying_down = findViewById(R.id.nowplaying_down);
        nowplaying_down.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            closeNowPlaying(null);
        });

        if (nowplaying_menu == null) nowplaying_menu = findViewById(R.id.nowplaying_menu);
        nowplaying_menu.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            openTrackMenu();
        });

        if (nowplaying_trackname == null) nowplaying_trackname = findViewById(R.id.nowplaying_trackname);
        nowplaying_trackname.setText(nowplaying_track.getTitle());
        nowplaying_trackname.setSelected(true);

        if (nowplaying_trackartist == null) nowplaying_trackartist = findViewById(R.id.nowplaying_trackartist);
        nowplaying_trackartist.setText(nowplaying_track.getArtist());

        if (nowplaying_navigation == null) nowplaying_navigation = findViewById(R.id.nowplaying_navigation);
        String nav_color = convert(sub_darken(nowplaying_track.getColor()));
        GradientDrawable d = (GradientDrawable) getDrawable(R.drawable.nowplaying_navbar);
        d.setColor(Color.parseColor(nav_color));
        nowplaying_navigation.setBackground(d);
        getWindow().setNavigationBarColor(Color.parseColor(nav_color));

        if (nowplaying_lyrics == null) nowplaying_lyrics = findViewById(R.id.nowplaying_lyricsbutton);
        if (nowplaying_track.hasLyrics()) nowplaying_lyrics.setTextColor(Color.parseColor("#FFFFFF"));
        else nowplaying_lyrics.setTextColor(Color.parseColor("#999999"));
        nowplaying_lyrics.setOnClickListener(view -> {
            if (!nowplaying_track.hasLyrics()) return;
            Common.vibrate(this, 50);
            startActivity(new Intent(this, LyricsActivity.class));
        });

        if (nowplaying_upnext == null) nowplaying_upnext = findViewById(R.id.nowplaying_upnextbutton);
        nowplaying_upnext.setOnClickListener(view -> {
            Common.vibrate(this, 50);
            launcher.launch(new Intent(this, QueueActivity.class));
//            startActivity(new Intent(this, QueueActivity.class));
        });

        if (nowplaying_seekbar == null) nowplaying_seekbar = findViewById(R.id.nowplaying_seekbar);
        nowplaying_seekbar.setSecondaryProgress(0);
        nowplaying_seekbar.setMax(0);
        nowplaying_seekbar_max_set = false;
        nowplaying_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int msec_progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int msec, boolean progressSet) {
                if (!progressSet) return;
                msec_progress = msec;
            };
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarOnFocus = true;
            };
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (music_service == null) return;
                music_service.seekTo(msec_progress);
                isSeekbarOnFocus = false;
            };
        });

        if (nowplaying_elapsedtime == null) nowplaying_elapsedtime = findViewById(R.id.nowplaying_elapsedtime);
        nowplaying_elapsedtime.setText("0:00");

        if (nowplaying_duration == null) nowplaying_duration = findViewById(R.id.nowplaying_duration);
        nowplaying_duration.setText("0:00");

//        if (nowplaying_fromtext == null) nowplaying_fromtext = findViewById(R.id.nowplaying_fromtext);
//        nowplaying_fromtext.setText("From " + nowplaying_track.getAlbum());
//        nowplaying_fromtext.setSelected(true);
//
//        if (nowplaying_yeartext == null) nowplaying_yeartext = findViewById(R.id.nowplaying_yeartext);
//        nowplaying_yeartext.setText(nowplaying_track.getYear());

        if (nowplaying_type == null) nowplaying_type = findViewById(R.id.nowplaying_type);
        nowplaying_type.setText(nowplaying_track.getType());

        if (nowplaying_year == null) nowplaying_year = findViewById(R.id.nowplaying_year);
        nowplaying_year.setText(nowplaying_track.getYear());

        if (nowplaying_toolbar_data == null) nowplaying_toolbar_data = findViewById(R.id.nowplaying_toolbar_data);
        GradientDrawable toolbar_d = (GradientDrawable) getDrawable(R.drawable.nowplaying_toolbar_data);
        toolbar_d.setColor(Color.parseColor(nav_color));
        nowplaying_toolbar_data.setBackground(toolbar_d);

        if (nowplaying_repeat == null) nowplaying_repeat = findViewById(R.id.nowplaying_repeat_icon);
        MusicForegroundService.NowPlayingData.REPEAT_TYPE currentRepeatType = MusicForegroundService.NowPlayingData.getInstance(getApplicationContext()).getRepeatType();
        if (currentRepeatType == MusicForegroundService.NowPlayingData.REPEAT_TYPE.NO_REPEAT) nowplaying_repeat.setImageResource(R.drawable.ic_no_repeat_icon);
        else if (currentRepeatType == MusicForegroundService.NowPlayingData.REPEAT_TYPE.REPEAT_CURRENT) nowplaying_repeat.setImageResource(R.drawable.ic_repeat_one_icon);
        else if (currentRepeatType == MusicForegroundService.NowPlayingData.REPEAT_TYPE.REPEAT_ALL) nowplaying_repeat.setImageResource(R.drawable.ic_repeat_all_icon);

        if (nowplaying_repeat_holder == null) nowplaying_repeat_holder = findViewById(R.id.nowplaying_repeat_icon_holder);
        nowplaying_repeat_holder.setOnClickListener(this::settingRepeatType);

        if (nowplaying_shuffle == null) nowplaying_shuffle = findViewById(R.id.nowplaying_shuffle_icon);
        if (nowplaying_shuffle_holder == null) nowplaying_shuffle_holder = findViewById(R.id.nowplaying_shuffle_icon_holder);
        nowplaying_shuffle_holder.setOnClickListener(this::shuffleAlgo);

    };

    private void shuffleAlgo(View v) {
        MusicForegroundService.NowPlayingData.getInstance(this).shuffleQueue();
        Toast.makeText(this, "Your queue is shuffled!", Toast.LENGTH_SHORT).show();
    };

    private void settingRepeatType(View v) {

        MusicForegroundService.NowPlayingData.REPEAT_TYPE currentRepeatType = MusicForegroundService.NowPlayingData.getInstance(getApplicationContext()).getRepeatType();

        if (currentRepeatType == MusicForegroundService.NowPlayingData.REPEAT_TYPE.NO_REPEAT) {
            nowplaying_repeat.setImageResource(R.drawable.ic_repeat_all_icon);
            MusicForegroundService.NowPlayingData.getInstance(getApplicationContext())
                .setRepeatType(MusicForegroundService.NowPlayingData.REPEAT_TYPE.REPEAT_ALL);
        }
        else if (currentRepeatType == MusicForegroundService.NowPlayingData.REPEAT_TYPE.REPEAT_CURRENT) {
            nowplaying_repeat.setImageResource(R.drawable.ic_no_repeat_icon);
            MusicForegroundService.NowPlayingData.getInstance(getApplicationContext())
                .setRepeatType(MusicForegroundService.NowPlayingData.REPEAT_TYPE.NO_REPEAT);
        }
        else if (currentRepeatType == MusicForegroundService.NowPlayingData.REPEAT_TYPE.REPEAT_ALL) {
            nowplaying_repeat.setImageResource(R.drawable.ic_repeat_one_icon);
            MusicForegroundService.NowPlayingData.getInstance(getApplicationContext())
                .setRepeatType(MusicForegroundService.NowPlayingData.REPEAT_TYPE.REPEAT_CURRENT);
        }

    };

    private void cleanup() {
        if (progressHandler != null && progressHandlerRunnable != null) {
            progressHandler.removeCallbacks(progressHandlerRunnable);
            progressHandler = null;
            progressHandlerRunnable = null;
        }
        MusicService.unBind(this, serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    };

    private void runProgressHandler() {

        progressHandler = new Handler();

        progressHandlerRunnable = new Runnable() {
            @Override
            public void run() {

                MusicForegroundService.NowPlayingData currentTrack = MusicForegroundService.NowPlayingData.getInstance(getApplicationContext());

                // initial play/pause button set
                if (!initialPlayPauseButtonSet && currentTrack.getMediaPrepared()) {
                    if (currentTrack.getMediaIsPlaying()) {
                        Log.d(tag, "setting pause button");
                        nowplaying_playbutton.setVisibility(View.GONE);
                        nowplaying_pausebutton.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(tag, "setting play button");
                        nowplaying_pausebutton.setVisibility(View.GONE);
                        nowplaying_playbutton.setVisibility(View.VISIBLE);
                    }
                    initialPlayPauseButtonSet = true;
                }

                // set duretion to seekbar
                if (
                        nowplaying_seekbar != null &&
                        music_service != null &&
                        !nowplaying_seekbar_max_set &&
                        currentTrack.getMediaPrepared()
                ) {
                    nowplaying_duration.setText(convertMilliSecondsToString(music_service.getDuration()));
                    nowplaying_seekbar.setMax(music_service.getDuration());
                    nowplaying_seekbar_max_set = true;
                }

                // set secondary progress to seekbar
                if (
                        nowplaying_seekbar != null &&
                        music_service != null &&
                        currentTrack.getMediaPrepared()
                ) {
                    int percent = currentTrack.getBufferedPercent();
                    int secondary_progress = (int) ((percent * music_service.getDuration()) / 100);
                    nowplaying_seekbar.setSecondaryProgress(secondary_progress);
                }

                // set current position to seekbar
                if (
                        nowplaying_seekbar != null &&
                        nowplaying_elapsedtime != null &&
                        music_service != null
                ) {
                    nowplaying_elapsedtime.setText(convertMilliSecondsToString(music_service.getCurrentPosition()));
                    if (!isSeekbarOnFocus) nowplaying_seekbar.setProgress(music_service.getCurrentPosition());
                }

                // check which button to show
                if (
                        music_service != null &&
                        music_service.isPlaying() &&
                        nowplaying_pausebutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    nowplaying_playbutton.setVisibility(View.GONE);
                    nowplaying_pausebutton.setVisibility(View.VISIBLE);
                }

                // check which button to show
                if (
                        music_service != null &&
                        !music_service.isPlaying() &&
                        nowplaying_playbutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    nowplaying_pausebutton.setVisibility(View.GONE);
                    nowplaying_playbutton.setVisibility(View.VISIBLE);
                }

                progressHandler.postDelayed(this, 100);

            }
        };

        progressHandler.postDelayed(progressHandlerRunnable, 100);

    };

    private static String convertMilliSecondsToString(int value) {
        int inSeconds = value / 1000;
        int mins = inSeconds / 60;
        int secs = inSeconds % 60;
        String n_secs = secs < 10 ? "0" + secs : secs + "";
        return mins + ":" + n_secs;
    };

    @Override
    public void onBackPressed() {
        closeNowPlaying(null);
        super.onBackPressed();
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
    };

    private static String getBottomColor(String color) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        double brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        if (brightness < 0.5) return lighten(color);
        else return darken(color);
    };

    private static String darken(String color) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        int nred = getInt(red, DARKEN_FACTOR);
        int ngreen = getInt(green, DARKEN_FACTOR);
        int nblue = getInt(blue, DARKEN_FACTOR);
        return "rgba(" + nred + "," + ngreen + "," + nblue + "," + cut[3] + ")";
    };

    private static String sub_darken(String color) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        int nred = getInt(red, SUB_DARKEN_FACTOR);
        int ngreen = getInt(green, SUB_DARKEN_FACTOR);
        int nblue = getInt(blue, SUB_DARKEN_FACTOR);
        return "rgba(" + nred + "," + ngreen + "," + nblue + "," + cut[3] + ")";
    };

    private static String lighten(String color) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        int nred = getInt(red, LIGHTEN_FACTOR);
        int ngreen = getInt(green, LIGHTEN_FACTOR);
        int nblue = getInt(blue, LIGHTEN_FACTOR);
        return "rgba(" + nred + "," + ngreen + "," + nblue + "," + cut[3] + ")";
    };

    private static int getInt(int value, double factor) {
        int num = (int) (value * factor);
        return Math.max(0, Math.min(num, 255));
    };

    private void closeNowPlaying(Intent intent) {
        setResult(1, intent);
        finish();
        overridePendingTransition(R.anim.previous_stay, R.anim.nowplaying_close);
    };

    private void openTrackMenu() {

        View.OnClickListener share = view -> {
            Common.vibrate(this, 50);
            Intent shareIntent = Common.shareTrack(nowplaying_track);
            if (shareIntent == null) return;
            startActivity(shareIntent);
        };

        View.OnClickListener goToHome = view -> {
            Intent intent = new Intent();
            intent.putExtra("album_id", nowplaying_track.getAlbumId());
            closeNowPlaying(intent);
        };

        List<BottomMenu.MenuItem> menu_list = Arrays.asList(
                new BottomMenu.MenuItem(null, "Go To Album", goToHome),
                new BottomMenu.MenuItem(null, "Share Track", share)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(this)
                .setDataList(menu_list)
                .setName(nowplaying_track.getTitle())
                .setArtist(nowplaying_track.getArtist())
                .setThumbnail(nowplaying_track.getThumbnail())
                .build();

        bottomMenu_new.show();

    };

};