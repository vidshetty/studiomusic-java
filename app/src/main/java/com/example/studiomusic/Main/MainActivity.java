package com.example.studiomusic.Main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.transition.Slide;
import android.transition.Transition;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studiomusic.Audio_Controller.MusicApplication;
import com.example.studiomusic.Audio_Controller.MusicForegroundService;
import com.example.studiomusic.Audio_Controller.MusicForegroundService.NowPlayingData;
import com.example.studiomusic.Audio_Controller.MusicServiceNew;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.FragAlbumView.Fragment_AlbumView;
import com.example.studiomusic.FragHomescreen.Fragment_Homescreen;
import com.example.studiomusic.FragLibrary.Fragment_Library;
import com.example.studiomusic.FragSearch.Fragment_Search;
import com.example.studiomusic.MusicData.Track;
import com.example.studiomusic.NowPlaying.NowPlaying;
import com.example.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "lifecycle";

    private LinearLayout mini_player = null;
    private ImageView miniplayer_albumart = null;
    private TextView miniplayer_title = null;
    private TextView miniplayer_artist = null;
    private ProgressBar mini_player_progressbar = null;
    private ConstraintLayout main_activity_parent = null;
    private GoogleSignInAccount account = null;
    private ProgressBar mini_player_loader = null;
    private ImageView mini_player_playbutton = null;
    private ImageView mini_player_pausebutton = null;
    private BottomNavigationView navigationView = null;
    private Fragment_Homescreen frag_home = new Fragment_Homescreen();
    private Fragment_Search frag_search = new Fragment_Search();
    private Fragment_Library frag_library = new Fragment_Library();

    private RelativeLayout mainRelativeLayout = null;
    private ConstraintLayout searchRelativeLayout = null;
    private PlayerBroadcastReceiver receiver = null;
    private PlaybackStopReceiver playbackStopReceiver = null;
    private MusicForegroundService music_service = null;
    private ServiceConnection serviceConnection = null;
    private Handler progressHandler = null;
    private Runnable progressHandlerRunnable = null;
    private ActivityResultLauncher<Intent> launcher = null;

    private boolean mini_player_progressbar_max_set = false;
    private boolean initialPlayPauseButtonSet = false;

    private class PlayerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mini_player == null) return;
            handleMiniPlayer();
        };
    };

    private class PlaybackStopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideMiniPlayer();
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        NowPlayingData.getInstance(this);

        mini_player_progressbar_max_set = false;

        receiver = new PlayerBroadcastReceiver();
        playbackStopReceiver = new PlaybackStopReceiver();

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

        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() != 1 || result.getData() == null) return;
                    String albumId = result.getData().getStringExtra("album_id");
                    openAlbumViewFragment(albumId);
                }
            }
        );

        setContentView(R.layout.activity_main);
        getWindow().setNavigationBarColor(getColor(R.color.light_black));
        getWindow().setStatusBarColor(getColor(R.color.light_black));

        account = GoogleSignIn.getLastSignedInAccount(this);
        navigationView = findViewById(R.id.bottom_nav_bar);

        main_activity_parent = findViewById(R.id.main_activity_parent);

        mini_player = findViewById(R.id.mini_player);
        miniplayer_albumart = findViewById(R.id.miniplayer_albumart);
        miniplayer_title = findViewById(R.id.miniplayer_title);
        miniplayer_artist = findViewById(R.id.miniplayer_artist);

        mini_player.setOnClickListener(view -> {
            Intent intent = new Intent(this, NowPlaying.class);
            launcher.launch(intent);
        });

        mini_player_loader = findViewById(R.id.mini_player_loader);
        mini_player_playbutton = findViewById(R.id.mini_player_playbutton);
        mini_player_playbutton.setOnClickListener(v -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            if (!music_service.isPlaying()) music_service.play_manual();
            mini_player_playbutton.setVisibility(View.GONE);
            mini_player_pausebutton.setVisibility(View.VISIBLE);
        });
        mini_player_pausebutton = findViewById(R.id.mini_player_pausebutton);
        mini_player_pausebutton.setOnClickListener(v -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            if (music_service.isPlaying()) music_service.pause_manual();
            mini_player_pausebutton.setVisibility(View.GONE);
            mini_player_playbutton.setVisibility(View.VISIBLE);
        });

        mini_player_progressbar = findViewById(R.id.mini_player_progressbar);

        mainRelativeLayout = findViewById(R.id.toolbar_layout);
        searchRelativeLayout = findViewById(R.id.search_toolbar);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_frame_layout, frag_home)
            .commit();

        navigationView.setOnItemSelectedListener(item -> {
            Fragment frag = getFragment(item.getItemId());
            String tag = getFragmentTag(item.getItemId());
            if (frag == null) return false;
            clearBackStack();
            setFragment(frag, tag);
            return true;
        });

        if (
                MusicServiceNew.isForegroundServiceRunning(this) &&
                NowPlayingData.getInstance(this).getQueueTrackList() != null &&
                NowPlayingData.getInstance(this).getQueueTrackList().size() > 0
        ) {
            handleMiniPlayer();
        }

        initialize();

    };

    private void sendBroadcast() {
        Intent intent = new Intent(MusicApplication.TRACK_CHANGE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    };

    private void initialize() {

        IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        IntentFilter intentFilter1 = new IntentFilter(MusicApplication.PLAYBACK_STOP);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStopReceiver, intentFilter1);

        MusicServiceNew.bind(this, serviceConnection);

        runProgressHandler();

    };

    private void runProgressHandler() {

        progressHandler = new Handler();

        progressHandlerRunnable = new Runnable() {
            @Override
            public void run() {

                NowPlayingData currentTrack = NowPlayingData.getInstance(getApplicationContext());

                // initial play/pause button set
                if (!initialPlayPauseButtonSet && currentTrack.getMediaPrepared()) {
                    mini_player_playbutton.setVisibility(View.GONE);
                    mini_player_pausebutton.setVisibility(View.VISIBLE);
                    mini_player_loader.setVisibility(View.GONE);
                    initialPlayPauseButtonSet = true;
                }

                // set duration to seekbar
                if (
                        mini_player_progressbar != null &&
                        music_service != null &&
                        !mini_player_progressbar_max_set &&
                        currentTrack.getMediaPrepared()
                ) {
                    mini_player_progressbar.setMax(music_service.getDuration());
                    mini_player_progressbar_max_set = true;
                }

                // set current position to seekbar
                if (
                        mini_player_progressbar != null &&
                        music_service != null
                ) {
                    mini_player_progressbar.setProgress(music_service.getCurrentPosition());
                }

                // check which button to show
                if (
                        music_service != null &&
                        music_service.isPlaying() &&
                        mini_player_pausebutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    mini_player_playbutton.setVisibility(View.GONE);
                    mini_player_pausebutton.setVisibility(View.VISIBLE);
                }

                // check which button to show
                if (
                        music_service != null &&
                        !music_service.isPlaying() &&
                        mini_player_playbutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    mini_player_pausebutton.setVisibility(View.GONE);
                    mini_player_playbutton.setVisibility(View.VISIBLE);
                }

                progressHandler.postDelayed(this, 100);

            }
        };

        progressHandler.postDelayed(progressHandlerRunnable, 100);

    };

    private Fragment getFragment(int id) {
        if (id == R.id.homescreen) return frag_home;
        if (id == R.id.search) return frag_search;
        if (id == R.id.library) return frag_library;
        return null;
    };

    private String getFragmentTag(int id) {
        if (id == R.id.homescreen) return "HOMESCREEN_FRAGMENT_TAG";
        if (id == R.id.search) return "SEARCH_FRAGMENT_TAG";
        if (id == R.id.library) return "LIBRARY_FRAGMENT_TAG";
        return null;
    };

    private void setFragment(Fragment fragment, String tag) {

        Fragment_AlbumView fragment_albumView = (Fragment_AlbumView) getSupportFragmentManager().findFragmentByTag("ALBUMVIEW_FRAGMENT_TAG");
        if (fragment_albumView != null) {
            getSupportFragmentManager().popBackStack();
        }

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_frame_layout, fragment, tag)
            .commit();

    };

    private void clearBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) return;
        for (int i=0; i<fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }
    };

    private String convert(String color) {
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

    private void showMiniPlayer() {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(500);
        transition.addTarget(mini_player);
        TransitionManager.beginDelayedTransition(main_activity_parent, transition);
        mini_player.setVisibility(View.VISIBLE);
    };

    private void hideMiniPlayer() {
        if (mini_player == null) return;
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(100);
        transition.addTarget(mini_player);
        TransitionManager.beginDelayedTransition(main_activity_parent, transition);
        mini_player.setVisibility(View.GONE);
    };

    private void handleMiniPlayer() {
        if (miniplayer_albumart == null) return;
        if (miniplayer_title == null) return;
        if (miniplayer_artist == null) return;
        if (mini_player_progressbar == null) return;
        if (mini_player_loader == null) return;
        if (mini_player_playbutton == null) return;
        if (mini_player_pausebutton == null) return;
        Track now_playing = NowPlayingData.getInstance(this).getTrack();
        Glide.with(this).asBitmap().load(now_playing.getThumbnail()).into(miniplayer_albumart);
        miniplayer_title.setText(now_playing.getTitle());
        miniplayer_artist.setText(now_playing.getArtist());
        mini_player_progressbar.setMax(0);
        mini_player_progressbar.setProgress(0);
        mini_player_progressbar_max_set = false;
        initialPlayPauseButtonSet = false;
        mini_player_loader.setVisibility(View.VISIBLE);
        mini_player_playbutton.setVisibility(View.GONE);
        mini_player_pausebutton.setVisibility(View.GONE);
        if (mini_player.getVisibility() == View.VISIBLE) return;
        showMiniPlayer();
    };

    private void openAlbumViewFragment(String albumId) {

        Fragment_AlbumView fragment = new Fragment_AlbumView();
        Bundle bundle = new Bundle();
        bundle.putString("album_id", albumId);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, fragment, "ALBUMVIEW_FRAGMENT_TAG")
                .addToBackStack(null)
                .commit();

    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        Fragment_Search search_fragment = (Fragment_Search) getSupportFragmentManager().findFragmentByTag("SEARCH_FRAGMENT_TAG");
        if (search_fragment == null) return super.dispatchTouchEvent(event);

        View fragment_view = search_fragment.getView();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            EditText et = fragment_view.findViewById(R.id.search_edittext);
            ImageView clear = fragment_view.findViewById(R.id.search_clear);
            Rect etRect = new Rect();
            et.getGlobalVisibleRect(etRect);
            Rect clearRect = new Rect();
            clear.getGlobalVisibleRect(clearRect);
            if (
                    !(etRect.contains((int)event.getRawX(), (int)event.getRawY()) ||
                    clearRect.contains((int)event.getRawX(), (int)event.getRawY()))
            ) {
                et.clearFocus();
                hideKeyboard(et.getWindowToken());
            }

//            View v = getCurrentFocus();
//            Log.d("instanceof", (v instanceof EditText) + "");
//            Log.d("instanceof", (v instanceof LinearLayout) + "");
//            if (v != null) Log.d("instanceof", getResources().getResourceName(v.getId()));
////            if (v != null) Log.d("instanceof", (v.getId() == R.id.search_toolbar) + " id check");
////            if (v != null) Log.d("instanceof", (v.getId() == R.id.search_edittext) + " id check");
//            if (v instanceof EditText) {
//                Rect outRect = new Rect();
//                v.getGlobalVisibleRect(outRect);
//                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
//                    v.clearFocus();
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
//            }

        }
        return super.dispatchTouchEvent(event);
    };

    private void hideKeyboard(IBinder token) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    };

    @Override
    protected void onDestroy() {
        cleanUp();
        super.onDestroy();
    };

    private void cleanUp() {
        if (progressHandler != null && progressHandlerRunnable != null) {
            progressHandler.removeCallbacks(progressHandlerRunnable);
            progressHandler = null;
            progressHandlerRunnable = null;
        }
        MusicServiceNew.unBind(this, serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackStopReceiver);
    };

};
