package com.app.studiomusic.NowPlayingNew;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.studiomusic.AppUpdates.UpdateChecker;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Lyrics.LyricsActivity;
import com.app.studiomusic.MusicData.QueueTrack;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.Queue.QueueActivity;
import com.app.studiomusic.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

public class NowPlayingNew extends AppCompatActivity {

//    private static final double DARKEN_FACTOR = 0.6;
//    private static final double SUB_DARKEN_FACTOR = 1;
//    private static final double LIGHTEN_FACTOR = 1.3;

    private static final double PARENT_DARKEN_FACTOR = 0.3;
    private static final double NAV_DARKEN_FACTOR = 0.5;
    private static final double DEFAULT_PARENT_FACTOR = 0.4;
    private static final double DEFAULT_NAV_FACTOR = 0.5;
    private static final String tag = "nowplaying_tag";
    private static final String color_tag = "nowplaying_color_tag";

    private Track previous_track = null;
    private Track nowplaying_track = null;
    private Integer light_color = null;
    private Integer dark_color = null;

    private ConstraintLayout nowplaying_parent = null;
    private ConstraintLayout nowplaying_navigation = null;
    private ConstraintLayout nowplaying_navigation_bar = null;
    private NowPlayingRecyclerView nowplaying_recyclerview = null;
    private NowPlaying_Adapter nowplaying_recylerview_adapter = null;
    private LinearLayoutManager nowplaying_recyclerview_layout = null;
    private ShapeableImageView nowplaying_down = null;
    private ConstraintLayout nowplaying_down_container = null;
    private ShapeableImageView nowplaying_menu = null;
    private ConstraintLayout nowplaying_menu_container = null;
    private ImageView nowplaying_playbutton = null;
    private ImageView nowplaying_pausebutton = null;
    private ProgressBar nowplaying_progressbar = null;
    private TextView nowplaying_trackname = null;
    private TextView nowplaying_trackartist = null;
    private TextView nowplaying_lyrics = null;
    private ConstraintLayout nowplaying_lyricsbutton_container = null;
    private Drawable nowplaying_lyrics_container_drawable = null;
    private TextView nowplaying_upnext = null;
    private ConstraintLayout nowplaying_upnext_container = null;
    private SeekBar nowplaying_seekbar = null;
    private TextView nowplaying_elapsedtime = null;
    private TextView nowplaying_duration = null;
    private ImageView nowplaying_previous_icon = null;
    private ConstraintLayout nowplaying_previous_icon_holder = null;
    private ImageView nowplaying_next_icon = null;
    private ConstraintLayout nowplaying_next_icon_holder = null;
    private TextView nowplaying_type = null;
    private TextView nowplaying_year = null;
    private LinearLayout nowplaying_toolbar_data = null;
    private ImageView nowplaying_repeat = null;
    private ConstraintLayout nowplaying_repeat_holder = null;
    private ImageView nowplaying_shuffle = null;
    private ConstraintLayout nowplaying_shuffle_holder = null;
    private ConstraintLayout nowplaying_gotoalbum = null;
    private ConstraintLayout nowplaying_sharetrack = null;
    private ConstraintLayout volume_down = null;
    private AppCompatSeekBar volume_seekbar = null;
    private ConstraintLayout volume_up = null;

    private boolean nowplaying_seekbar_max_set = false;
    private boolean isSeekbarOnFocus = false;
    private boolean initialPlayPauseButtonSet = false;

    private Handler progressHandler = null;
    private MusicForegroundService music_service = null;
    private Runnable progressHandlerRunnable = null;
    private CurrentTrackReceiver receiver = null;
    private QueueTrackSwapReceiver queueTrackSwapReceiver = null;
    private TrackRemoveReceiver trackRemoveReceiver = null;
    private RadioResponseReceiver radioResponseReceiver = null;
    private UpdateReceiver updateReceiver = null;
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

    private class QueueTrackSwapReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position1 = intent.getIntExtra("position1", -1);
            int position2 = intent.getIntExtra("position2", -1);
            if (position1 == -1 || position2 == -1) return;
            if (nowplaying_recylerview_adapter == null) return;
            nowplaying_recylerview_adapter.notifyItemChanged(position1);
            nowplaying_recylerview_adapter.notifyItemChanged(position2);
            nowplaying_recyclerview.scrollToPosition(MusicForegroundService.NowPlayingData.getInstance(NowPlayingNew.this).getIndex());
        };
    };

    private class TrackRemoveReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            int position = intent.getIntExtra("position", -1);
            if (position == -1) return;
            if (nowplaying_recylerview_adapter == null) return;
            nowplaying_recylerview_adapter.notifyItemRemoved(position);
            int nowplaying_position = MusicForegroundService.NowPlayingData.getInstance(NowPlayingNew.this).getIndex();
            nowplaying_recyclerview.scrollToPosition(nowplaying_position);
        };
    };

    private class RadioResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onRadioResponse();
        };
    };

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateChecker.install(NowPlayingNew.this);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_now_playing_new);
        overridePendingTransition(R.anim.nowplaying_open, R.anim.previous_stay);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        previous_track = null;
        nowplaying_track = null;

        initialize();
        createView();

    };

    @Override
    protected void onNewIntent(Intent root_intent) {
        super.onNewIntent(root_intent);
        String root_tag = "nowplaying_root";
        if (root_intent != null) {
            Log.d(root_tag, root_intent.getAction() + " ");
            Log.d(root_tag, root_intent.getDataString() + " ");
            Log.d(root_tag, root_intent.getStringExtra("open_now_playing") + " ");
        }
    }

    @Override
    protected void onDestroy() {
        onDestroyCleanup();
        super.onDestroy();
    };

    private void initialize() {

        if (receiver == null) {
            IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_CHANGE);
            receiver = new CurrentTrackReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
        }

        if (queueTrackSwapReceiver == null) {
            IntentFilter intentFilter = new IntentFilter(MusicApplication.QUEUE_TRACK_SWAP);
            queueTrackSwapReceiver = new QueueTrackSwapReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(queueTrackSwapReceiver, intentFilter);
        }

        if (trackRemoveReceiver == null) {
            IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_REMOVE);
            trackRemoveReceiver = new TrackRemoveReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(trackRemoveReceiver, intentFilter);
        }

        if (radioResponseReceiver == null) {
            IntentFilter intentFilter = new IntentFilter(MusicApplication.RADIO_RESPONSE);
            radioResponseReceiver = new RadioResponseReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(radioResponseReceiver, intentFilter);
        }

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

        previous_track = nowplaying_track;
        nowplaying_track = MusicForegroundService.NowPlayingData.getInstance(this).getTrack();

//        String hexColor = convert(darken(nowplaying_track.getColor()));

//        getWindow().setStatusBarColor(Color.parseColor(hexColor));

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
        int height = Common.getStatusBarHeight(this);
        nowplaying_parent.setPadding(0, height, 0, 0);

//        if (nowplaying_thumbnail == null) nowplaying_thumbnail = findViewById(R.id.nowplaying_thumbnail);
//        Glide.with(this).asBitmap().load(nowplaying_track.getThumbnail()).into(nowplaying_thumbnail);

        if (nowplaying_recyclerview == null) {
            nowplaying_recyclerview = findViewById(R.id.nowplaying_recyclerview);
            nowplaying_recyclerview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (nowplaying_recyclerview.getHeight() <= 0) return;

                    int h = nowplaying_recyclerview.getHeight();
                    int h1 = nowplaying_recyclerview.getLayoutParams().height;
                    Log.d("recyclerview_height", h + " " + h1);
                    nowplaying_recyclerview.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    Context context = NowPlayingNew.this;
                    nowplaying_recyclerview.setScrollEnabled(false);
                    PagerSnapHelper snap = new PagerSnapHelper();
                    snap.attachToRecyclerView(nowplaying_recyclerview);
                    List<QueueTrack> queue_tracks = MusicForegroundService.NowPlayingData.getInstance(context).getQueueTrackList();
                    nowplaying_recylerview_adapter = new NowPlaying_Adapter(context, queue_tracks);
                    nowplaying_recyclerview_layout = new LinearLayoutManager(context);
                    nowplaying_recyclerview_layout.setOrientation(LinearLayoutManager.HORIZONTAL);
                    nowplaying_recyclerview.setLayoutManager(nowplaying_recyclerview_layout);
                    nowplaying_recyclerview.setAdapter(nowplaying_recylerview_adapter);
                    nowplaying_recyclerview.scrollToPosition(MusicForegroundService.NowPlayingData.getInstance(context).getIndex());

                }
            });
        }
        else {

            int old_position = -1;
            List<QueueTrack> queue = MusicForegroundService.NowPlayingData.getInstance(this).getQueueTrackList();
            for (int i=0; i<queue.size(); i++) {
                if (previous_track != null && previous_track.getTrackId().equals(queue.get(i).getTrackId())) {
                    old_position = i;
                    break;
                }
            }

            int new_position = MusicForegroundService.NowPlayingData.getInstance(this).getIndex();

            if (
                (old_position == queue.size()-1 && new_position == 0) ||
                (old_position == 0 && new_position == queue.size()-1) ||
                Math.abs(new_position - old_position) > 1
            ) nowplaying_recyclerview.scrollToPosition(new_position);
            else nowplaying_recyclerview.smoothScrollToPosition(new_position);

        }

        if (nowplaying_playbutton == null) nowplaying_playbutton = findViewById(R.id.nowplaying_playbutton);
        nowplaying_playbutton.setOnClickListener(view -> {
            Common.vibrate(this, 10);
            if (music_service == null) return;
            if (!music_service.isPlaying()) music_service.play_manual();
            nowplaying_playbutton.setVisibility(View.GONE);
            nowplaying_pausebutton.setVisibility(View.VISIBLE);
        });

        if (nowplaying_pausebutton == null) nowplaying_pausebutton = findViewById(R.id.nowplaying_pausebutton);
        nowplaying_pausebutton.setOnClickListener(view -> {
            Common.vibrate(this, 10);
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
        if (nowplaying_previous_icon_holder == null) nowplaying_previous_icon_holder = findViewById(R.id.nowplaying_previous_icon_holder);
        nowplaying_previous_icon_holder.setOnClickListener(view -> {
//            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.playPreviousTrack();
        });
        nowplaying_previous_icon_holder.setOnLongClickListener(view -> {
            if (music_service == null) return false;
            music_service.rewind();
            return true;
        });

        if (nowplaying_next_icon == null) nowplaying_next_icon = findViewById(R.id.nowplaying_next_icon);
        if (nowplaying_next_icon_holder == null) nowplaying_next_icon_holder = findViewById(R.id.nowplaying_next_icon_holder);
        nowplaying_next_icon_holder.setOnClickListener(view -> {
//            Common.vibrate(this, 50);
            if (music_service == null) return;
            music_service.playNextTrack();
        });
        nowplaying_next_icon_holder.setOnLongClickListener(view -> {
            if (music_service == null) return false;
            music_service.forward();
            return true;
        });

        if (nowplaying_down == null) nowplaying_down = findViewById(R.id.nowplaying_down);
        if (nowplaying_down_container == null) nowplaying_down_container = findViewById(R.id.nowplaying_down_container);
        nowplaying_down_container.setOnClickListener(view -> {
//            Common.vibrate(this, 50);
            new Handler().postDelayed(() -> {
                closeNowPlaying(null);
            }, 100);
        });

        if (nowplaying_menu == null) nowplaying_menu = findViewById(R.id.nowplaying_menu);
        if (nowplaying_menu_container == null) nowplaying_menu_container = findViewById(R.id.nowplaying_menu_container);
        nowplaying_menu_container.setOnClickListener(view -> {
//            Common.vibrate(this, 50);
            new Handler().postDelayed(() -> {
                openTrackMenu();
            }, 100);
        });

        if (nowplaying_trackname == null) nowplaying_trackname = findViewById(R.id.nowplaying_trackname);
        nowplaying_trackname.setText(nowplaying_track.getTitle());
        nowplaying_trackname.setSelected(true);

        if (nowplaying_trackartist == null) nowplaying_trackartist = findViewById(R.id.nowplaying_trackartist);
        nowplaying_trackartist.setText(nowplaying_track.getArtist());

        if (nowplaying_navigation == null) nowplaying_navigation = findViewById(R.id.nowplaying_navigation);

        if (nowplaying_navigation_bar == null) nowplaying_navigation_bar = findViewById(R.id.nowplaying_navigation_bar);
        ViewGroup.LayoutParams params = nowplaying_navigation_bar.getLayoutParams();
        params.height = Common.getNavigationBarHeight(this);
        nowplaying_navigation_bar.setLayoutParams(params);

        if (nowplaying_lyrics == null) nowplaying_lyrics = findViewById(R.id.nowplaying_lyricsbutton);
        if (nowplaying_lyricsbutton_container == null) nowplaying_lyricsbutton_container = findViewById(R.id.nowplaying_lyricsbutton_container);
        if (nowplaying_lyrics_container_drawable == null) nowplaying_lyrics_container_drawable = nowplaying_lyricsbutton_container.getBackground();
        if (nowplaying_track.hasLyrics()) {
            nowplaying_lyrics.setTextColor(Color.parseColor("#FFFFFF"));
            nowplaying_lyricsbutton_container.setClickable(true);
            nowplaying_lyricsbutton_container.setBackground(nowplaying_lyrics_container_drawable);
        }
        else {
            nowplaying_lyrics.setTextColor(Color.parseColor("#999999"));
            nowplaying_lyricsbutton_container.setClickable(false);
            nowplaying_lyricsbutton_container.setBackground(null);
        }
        nowplaying_lyricsbutton_container.setOnClickListener(view -> {
            if (!nowplaying_track.hasLyrics()) return;
//            Common.vibrate(this, 50);
            startActivity(new Intent(this, LyricsActivity.class));
        });

        if (nowplaying_upnext == null) nowplaying_upnext = findViewById(R.id.nowplaying_upnextbutton);
        if (nowplaying_upnext_container == null) nowplaying_upnext_container = findViewById(R.id.nowplaying_upnextbutton_container);
        nowplaying_upnext_container.setOnClickListener(view -> {
//            Common.vibrate(this, 50);
            launcher.launch(new Intent(this, QueueActivity.class));
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
        nowplaying_toolbar_data.setBackgroundColor(Color.WHITE);

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

        if (nowplaying_gotoalbum == null) nowplaying_gotoalbum = findViewById(R.id.nowplaying_gotoalbum);
        nowplaying_gotoalbum.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("album_id", nowplaying_track.getAlbumId());
            closeNowPlaying(intent);
        });
        if (nowplaying_sharetrack == null) nowplaying_sharetrack = findViewById(R.id.nowplaying_sharetrack);
        nowplaying_sharetrack.setOnClickListener(v -> {
            Intent shareIntent = Common.shareTrack(nowplaying_track);
            if (shareIntent == null) return;
            startActivity(shareIntent);
        });

        updateColors_v2();

//        setupVolume();

//        if (previous_track == null) {
//            if (
//                nowplaying_track.getLightColor() == null ||
//                nowplaying_track.getDarkColor() == null
//            ) updateColors();
//            else updateColors_1();
//        }
//        else {
//            if (
//                previous_track.getLightColor() == null ||
//                previous_track.getDarkColor() == null ||
//                nowplaying_track.getLightColor() == null ||
//                nowplaying_track.getDarkColor() == null
//            ) updateColors();
//            else updateColors_1();
//        }

    };

//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
////        nowplaying_recylerview_adapter.notifyDataSetChanged();
//        nowplaying_recyclerview.scrollToPosition(NowPlayingData.getInstance(this).getIndex());
//    };

//    private void setupVolume() {
//
//        volume_down = findViewById(R.id.volume_down_ripple);
//        volume_seekbar = findViewById(R.id.nowplaying_volume_seekbar);
//        volume_up = findViewById(R.id.volume_up_ripple);
//
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//        if (audioManager == null) return;
//
//        final int MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//
//        volume_seekbar.setMax(MAX_VOLUME);
//        volume_seekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
//        volume_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            private int msec_progress = 0;
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int msec, boolean progressSet) {
//                if (!progressSet) return;
//                msec_progress = msec;
//            };
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {};
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                volume_seekbar.setProgress(msec_progress);
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, msec_progress, 0);
//            };
//        });
//
//        volume_down.setOnClickListener(v -> {
//            int new_volume = Math.max(0, volume_seekbar.getProgress() - 1);
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, new_volume, 0);
//            volume_seekbar.setProgress(new_volume);
//        });
//
//        volume_up.setOnClickListener(v -> {
//            int new_volume = Math.min(volume_seekbar.getProgress() + 1, MAX_VOLUME);
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, new_volume, 0);
//            volume_seekbar.setProgress(new_volume);
//        });
//
//    };

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        int action = event.getAction();
//        int keyCode = event.getKeyCode();
//
//        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
//            return super.dispatchKeyEvent(event);
//        }
//
//        if (action == KeyEvent.ACTION_UP) {
//            return super.dispatchKeyEvent(event);
//        }
//
//        if (volume_seekbar == null) {
//            return super.dispatchKeyEvent(event);
//        }
//
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//        if (audioManager == null) {
//            return super.dispatchKeyEvent(event);
//        }
//
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) volume_seekbar.setProgress(volume_seekbar.getProgress() - 1);
//        else volume_seekbar.setProgress(volume_seekbar.getProgress() + 1);
//
//        return super.dispatchKeyEvent(event);
//
//    };

    private void updateColors() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        if (
            previous_track == null
        ) {

            Log.d(color_tag, "getColor() " + nowplaying_track.getColor());

            int parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
            int nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);

            Log.d(color_tag, "parent color " + parent_color);
            Log.d(color_tag, "nav color " + nav_color);

            nowplaying_parent.setBackgroundColor(parent_color);
            nowplaying_navigation.setBackgroundColor(nav_color);
            nowplaying_navigation_bar.setBackgroundColor(nav_color);
//            nowplaying_toolbar_data.setBackgroundColor(nav_color);

        }
        else {

            Log.d(color_tag, "prev getColor() " + previous_track.getColor());
            Log.d(color_tag, "current getColor() " + nowplaying_track.getColor());

            int prev_parent_color = Common.convert(previous_track.getColor(), PARENT_DARKEN_FACTOR);
            int current_parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);

            int prev_nav_color = Common.convert(previous_track.getColor(), NAV_DARKEN_FACTOR);
            int current_nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);

            Log.d(color_tag, "prev parent color " + prev_parent_color);
            Log.d(color_tag, "prev nav color " + prev_nav_color);
            Log.d(color_tag, "current parent color " + current_parent_color);
            Log.d(color_tag, "current nav color " + current_nav_color);

            int[] prev_parent_colors = {prev_parent_color, prev_parent_color};
            int[] current_parent_colors = {current_parent_color, current_parent_color};
            GradientDrawable prev_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors);
            GradientDrawable current_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors);
            TransitionDrawable parent_trans = new TransitionDrawable(new GradientDrawable[] {prev_parent, current_parent});
            nowplaying_parent.setBackground(parent_trans);

            int[] prev_nav_colors_1 = {prev_nav_color, prev_nav_color};
            int[] current_nav_colors_1 = {current_nav_color, current_nav_color};
            GradientDrawable prev_nav_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_1);
            GradientDrawable current_nav_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_1);
            TransitionDrawable nav_trans_1 = new TransitionDrawable(new GradientDrawable[] {prev_nav_1, current_nav_1});
            nowplaying_navigation.setBackground(nav_trans_1);

            int[] prev_nav_colors_2 = {prev_nav_color, prev_nav_color};
            int[] current_nav_colors_2 = {current_nav_color, current_nav_color};
            GradientDrawable prev_nav_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_2);
            GradientDrawable current_nav_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_2);
            TransitionDrawable nav_trans_2 = new TransitionDrawable(new GradientDrawable[] {prev_nav_2, current_nav_2});
            nowplaying_navigation_bar.setBackground(nav_trans_2);

//            int[] prev_nav_colors_3 = {prev_nav_color, prev_nav_color};
//            int[] current_nav_colors_3 = {current_nav_color, current_nav_color};
//            GradientDrawable prev_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_3);
//            GradientDrawable current_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_3);
//            TransitionDrawable nav_trans_3 = new TransitionDrawable(new GradientDrawable[] {prev_nav_3, current_nav_3});
//            nowplaying_toolbar_data.setBackground(nav_trans_3);

            parent_trans.startTransition(500);
            nav_trans_1.startTransition(500);
            nav_trans_2.startTransition(500);
//            nav_trans_3.startTransition(500);

        }

        Log.d(color_tag, "------------------------");

    };

    private void updateColors_1() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        if (
            previous_track == null
        ) {

            Log.d(color_tag, "getLightColor() " + nowplaying_track.getLightColor());
            Log.d(color_tag, "getDarkColor() " + nowplaying_track.getDarkColor());

            int parent_color = Common.convert(nowplaying_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
            int nav_color = Common.convert(nowplaying_track.getLightColor(), DEFAULT_NAV_FACTOR);

            Log.d(color_tag, "parent color " + parent_color);
            Log.d(color_tag, "nav color " + nav_color);

            nowplaying_parent.setBackgroundColor(parent_color);
            nowplaying_navigation.setBackgroundColor(nav_color);
            nowplaying_navigation_bar.setBackgroundColor(nav_color);
//            nowplaying_toolbar_data.setBackgroundColor(nav_color);

        }
        else {

            Log.d(color_tag, "prev getLightColor() " + previous_track.getLightColor());
            Log.d(color_tag, "prev getDarkColor() " + previous_track.getDarkColor());
            Log.d(color_tag, "current getLightColor() " + nowplaying_track.getLightColor());
            Log.d(color_tag, "current getLightColor() " + nowplaying_track.getDarkColor());

            int prev_parent_color = Common.convert(previous_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
            int current_parent_color = Common.convert(nowplaying_track.getDarkColor(), DEFAULT_PARENT_FACTOR);

            int prev_nav_color = Common.convert(previous_track.getLightColor(), DEFAULT_NAV_FACTOR);
            int current_nav_color = Common.convert(nowplaying_track.getLightColor(), DEFAULT_NAV_FACTOR);

            Log.d(color_tag, "prev parent " + prev_parent_color);
            Log.d(color_tag, "prev nav " + prev_nav_color);
            Log.d(color_tag, "current parent " + current_parent_color);
            Log.d(color_tag, "current nav " + current_nav_color);

            int[] prev_parent_colors = {prev_parent_color, prev_parent_color};
            int[] current_parent_colors = {current_parent_color, current_parent_color};
            GradientDrawable prev_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors);
            GradientDrawable current_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors);
            TransitionDrawable parent_trans = new TransitionDrawable(new GradientDrawable[] {prev_parent, current_parent});
            nowplaying_parent.setBackground(parent_trans);

            int[] prev_nav_colors_1 = {prev_nav_color, prev_nav_color};
            int[] current_nav_colors_1 = {current_nav_color, current_nav_color};
            GradientDrawable prev_nav_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_1);
            GradientDrawable current_nav_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_1);
            TransitionDrawable nav_trans_1 = new TransitionDrawable(new GradientDrawable[] {prev_nav_1, current_nav_1});
            nowplaying_navigation.setBackground(nav_trans_1);

            int[] prev_nav_colors_2 = {prev_nav_color, prev_nav_color};
            int[] current_nav_colors_2 = {current_nav_color, current_nav_color};
            GradientDrawable prev_nav_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_2);
            GradientDrawable current_nav_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_2);
            TransitionDrawable nav_trans_2 = new TransitionDrawable(new GradientDrawable[] {prev_nav_2, current_nav_2});
            nowplaying_navigation_bar.setBackground(nav_trans_2);

//            int[] prev_nav_colors_3 = {prev_nav_color, prev_nav_color};
//            int[] current_nav_colors_3 = {current_nav_color, current_nav_color};
//            GradientDrawable prev_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_3);
//            GradientDrawable current_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_3);
//            TransitionDrawable nav_trans_3 = new TransitionDrawable(new GradientDrawable[] {prev_nav_3, current_nav_3});
//            nowplaying_toolbar_data.setBackground(nav_trans_3);

            parent_trans.startTransition(500);
            nav_trans_1.startTransition(500);
            nav_trans_2.startTransition(500);
//            nav_trans_3.startTransition(500);

        }

        Log.d(color_tag, "------------------------");

    };

    private void updateColors_v2() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        if (previous_track == null) {

            Log.d(color_tag, "nowplaying lightcolor " + nowplaying_track.getLightColor() + " " + "null".equals(nowplaying_track.getLightColor()));
            Log.d(color_tag, "nowplaying darkcolor " + nowplaying_track.getDarkColor());
            Log.d(color_tag, "nowplaying color " + nowplaying_track.getColor());

            int parent_color = -1;
            int nav_color = -1;
//
            if (
                    nowplaying_track.getLightColor() != null &&
                    nowplaying_track.getDarkColor() != null
            ) {
                parent_color = Common.convert(nowplaying_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
                nav_color = Common.convert(nowplaying_track.getLightColor(), DEFAULT_NAV_FACTOR);
            }
            else {
                parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
                nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);
            }

//            parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
//            nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);

            nowplaying_parent.setBackgroundColor(parent_color);
            nowplaying_navigation.setBackgroundColor(nav_color);
            nowplaying_navigation_bar.setBackgroundColor(nav_color);
//            nowplaying_toolbar_data.setBackgroundColor(nav_color);

        }
        else {

            Log.d(color_tag, "nowplaying lightcolor " + nowplaying_track.getLightColor());
            Log.d(color_tag, "nowplaying darkcolor " + nowplaying_track.getDarkColor());
            Log.d(color_tag, "nowplaying color " + nowplaying_track.getColor());
            Log.d(color_tag, "prev lightcolor " + previous_track.getLightColor());
            Log.d(color_tag, "prev darkcolor " + previous_track.getDarkColor());
            Log.d(color_tag, "prev color " + previous_track.getColor());

            int prev_parent_color = -1;
            int prev_nav_color = -1;
            int current_parent_color = -1;
            int current_nav_color = -1;

            if (previous_track.getLightColor() != null && previous_track.getDarkColor() != null) {
                prev_parent_color = Common.convert(previous_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
                prev_nav_color = Common.convert(previous_track.getLightColor(), DEFAULT_NAV_FACTOR);
            }
            else {
                prev_parent_color = Common.convert(previous_track.getColor(), PARENT_DARKEN_FACTOR);
                prev_nav_color = Common.convert(previous_track.getColor(), NAV_DARKEN_FACTOR);
            }

//            prev_parent_color = Common.convert(previous_track.getColor(), PARENT_DARKEN_FACTOR);
//            prev_nav_color = Common.convert(previous_track.getColor(), NAV_DARKEN_FACTOR);

            if (nowplaying_track.getLightColor() != null && nowplaying_track.getDarkColor() != null) {
                current_parent_color = Common.convert(nowplaying_track.getDarkColor(), DEFAULT_PARENT_FACTOR);
                current_nav_color = Common.convert(nowplaying_track.getLightColor(), DEFAULT_NAV_FACTOR);
            }
            else {
                current_parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
                current_nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);
            }

//            current_parent_color = Common.convert(nowplaying_track.getColor(), PARENT_DARKEN_FACTOR);
//            current_nav_color = Common.convert(nowplaying_track.getColor(), NAV_DARKEN_FACTOR);

            int[] prev_parent_colors = {prev_parent_color, prev_parent_color};
            int[] current_parent_colors = {current_parent_color, current_parent_color};
            GradientDrawable prev_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_parent_colors);
            GradientDrawable current_parent = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_parent_colors);
            TransitionDrawable parent_trans = new TransitionDrawable(new GradientDrawable[] {prev_parent, current_parent});
            nowplaying_parent.setBackground(parent_trans);

            int[] prev_nav_colors_1 = {prev_nav_color, prev_nav_color};
            int[] current_nav_colors_1 = {current_nav_color, current_nav_color};
            GradientDrawable prev_nav_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_1);
            GradientDrawable current_nav_1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_1);
            TransitionDrawable nav_trans_1 = new TransitionDrawable(new GradientDrawable[] {prev_nav_1, current_nav_1});
            nowplaying_navigation.setBackground(nav_trans_1);

            int[] prev_nav_colors_2 = {prev_nav_color, prev_nav_color};
            int[] current_nav_colors_2 = {current_nav_color, current_nav_color};
            GradientDrawable prev_nav_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_2);
            GradientDrawable current_nav_2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_2);
            TransitionDrawable nav_trans_2 = new TransitionDrawable(new GradientDrawable[] {prev_nav_2, current_nav_2});
            nowplaying_navigation_bar.setBackground(nav_trans_2);

//            int[] prev_nav_colors_3 = {prev_nav_color, prev_nav_color};
//            int[] current_nav_colors_3 = {current_nav_color, current_nav_color};
//            GradientDrawable prev_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, prev_nav_colors_3);
//            GradientDrawable current_nav_3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, current_nav_colors_3);
//            TransitionDrawable nav_trans_3 = new TransitionDrawable(new GradientDrawable[] {prev_nav_3, current_nav_3});
//            nowplaying_toolbar_data.setBackground(nav_trans_3);

            parent_trans.startTransition(300);
            nav_trans_1.startTransition(300);
            nav_trans_2.startTransition(300);
//            nav_trans_3.startTransition(300);

        }

        Log.d(color_tag, "------------------------");

    };

    private void onRadioResponse() {
        if (nowplaying_recyclerview == null) return;
        List<QueueTrack> tracks = MusicForegroundService.NowPlayingData.getInstance(this).getQueueTrackList();
        nowplaying_recylerview_adapter = new NowPlaying_Adapter(this, tracks);
        nowplaying_recyclerview.setAdapter(nowplaying_recylerview_adapter);
        nowplaying_recyclerview.scrollToPosition(0);
    };

    private void shuffleAlgo(View v) {
        MusicForegroundService.NowPlayingData.getInstance(this).shuffleQueue();
        List<QueueTrack> list = MusicForegroundService.NowPlayingData.getInstance(this).getQueueTrackList();
        nowplaying_recylerview_adapter = new NowPlaying_Adapter(this, list);
        nowplaying_recyclerview.setAdapter(nowplaying_recylerview_adapter);
        nowplaying_recyclerview.scrollToPosition(0);
        Snackbar bar = Snackbar.make(this, nowplaying_parent, "Your queue is now shuffled!", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.white))
                .setTextColor(getColor(R.color.black))
                .setDuration(2000)
                .setAnchorView(nowplaying_navigation);
        bar.setAction("Clear", view -> {
            bar.dismiss();
        });
        bar.show();
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
    };

    private void onDestroyCleanup() {
        cleanup();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queueTrackSwapReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(trackRemoveReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(radioResponseReceiver);
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

//    private static String convert(String color) {
//        String[] cut = color.substring(5,color.length()-1).split(",");
//        int red = Integer.parseInt(cut[0]);
//        int green = Integer.parseInt(cut[1]);
//        int blue = Integer.parseInt(cut[2]);
//        int nred = (int) ((int) red + (-red * 0.5));
//        int ngreen = (int) ((int) green + (-green * 0.5));
//        int nblue = (int) ((int) blue + (-blue * 0.5));
//        String hex = String.format("#%02x%02x%02x", nred, ngreen, nblue);
//        return hex;
//    };
//
//    private static int convert(String color, double percent) {
//        String[] cut = color.substring(5,color.length()-1).split(",");
//        int red = Integer.parseInt(cut[0]);
//        int green = Integer.parseInt(cut[1]);
//        int blue = Integer.parseInt(cut[2]);
//        return Color.rgb((int) (red * percent), (int) (green * percent), (int) (blue * percent));
//    };
//
//    private static String getBottomColor(String color) {
//        String[] cut = color.substring(5,color.length()-1).split(",");
//        int red = Integer.parseInt(cut[0]);
//        int green = Integer.parseInt(cut[1]);
//        int blue = Integer.parseInt(cut[2]);
//        double brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
//        if (brightness < 0.5) return lighten(color);
//        else return darken(color);
//    };
//
//    private static String darken(String color) {
//        String[] cut = color.substring(5,color.length()-1).split(",");
//        int red = Integer.parseInt(cut[0]);
//        int green = Integer.parseInt(cut[1]);
//        int blue = Integer.parseInt(cut[2]);
//        int nred = getInt(red, DARKEN_FACTOR);
//        int ngreen = getInt(green, DARKEN_FACTOR);
//        int nblue = getInt(blue, DARKEN_FACTOR);
//        return "rgba(" + nred + "," + ngreen + "," + nblue + "," + cut[3] + ")";
//    };
//
//    private static String sub_darken(String color) {
//        String[] cut = color.substring(5,color.length()-1).split(",");
//        int red = Integer.parseInt(cut[0]);
//        int green = Integer.parseInt(cut[1]);
//        int blue = Integer.parseInt(cut[2]);
//        int nred = getInt(red, SUB_DARKEN_FACTOR);
//        int ngreen = getInt(green, SUB_DARKEN_FACTOR);
//        int nblue = getInt(blue, SUB_DARKEN_FACTOR);
//        return "rgba(" + nred + "," + ngreen + "," + nblue + "," + cut[3] + ")";
//    };
//
//    private static String lighten(String color) {
//        String[] cut = color.substring(5,color.length()-1).split(",");
//        int red = Integer.parseInt(cut[0]);
//        int green = Integer.parseInt(cut[1]);
//        int blue = Integer.parseInt(cut[2]);
//        int nred = getInt(red, LIGHTEN_FACTOR);
//        int ngreen = getInt(green, LIGHTEN_FACTOR);
//        int nblue = getInt(blue, LIGHTEN_FACTOR);
//        return "rgba(" + nred + "," + ngreen + "," + nblue + "," + cut[3] + ")";
//    };

//    private static int getInt(int value, double factor) {
//        int num = (int) (value * factor);
//        return Math.max(0, Math.min(num, 255));
//    };

    private void closeNowPlaying(Intent intent) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
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

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        super.onPause();
    };

    @Override
    protected void onPostResume() {
        IntentFilter intentFilter = new IntentFilter(MusicApplication.UPDATE_DOWNLOAD);
        if (updateReceiver == null) updateReceiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter);
        super.onPostResume();
    };

};