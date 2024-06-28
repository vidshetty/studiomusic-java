package com.app.studiomusic.Main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.TransitionManager;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.transition.Slide;
import android.transition.Transition;
import android.widget.TextView;

import com.app.studiomusic.AppUpdates.UpdateChecker;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.SnackBar;
import com.app.studiomusic.FragAlbumView.Fragment_AlbumView;
import com.app.studiomusic.FragHomescreen.Fragment_Homescreen;
import com.app.studiomusic.FragLibrary.Fragment_Library;
import com.app.studiomusic.FragMoreSearch.Fragment_More_Search;
import com.app.studiomusic.FragSearch.Fragment_Search;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.NowPlayingNew.NowPlayingNew;
import com.bumptech.glide.Glide;
import com.app.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "lifecycle";
    private static final int NAVIGATION_NON_SELECTED_COLOR = R.color.transparent;
    private static final int NAVIGATION_SELECTED_COLOR = R.color.primary;
    private static String CURRENT_SCREEN = null;

    private Intent root_intent = null;
    private String albumview_albumid = null;
    private boolean albumview_is_playable = false;
    private String albumview_trackid = null;

    private LinearLayout mini_player = null;
    private ImageView miniplayer_albumart = null;
    private TextView miniplayer_title = null;
    private TextView miniplayer_artist = null;
    private ProgressBar mini_player_progressbar = null;
    private ConstraintLayout main_activity_parent = null;
    private GoogleSignInAccount account = null;
    private ProgressBar mini_player_loader = null;
    private ShapeableImageView mini_player_playbutton = null;
    private ShapeableImageView mini_player_pausebutton = null;
    private ConstraintLayout mini_player_play_pause_ripple_container = null;
    private ConstraintLayout mini_player_play_pause_container = null;
//    private BottomNavigationView navigationView = null;

    private ConstraintLayout main_navigation_home = null;
    private ConstraintLayout main_navigation_home_image_container = null;
    private ConstraintLayout main_navigation_search = null;
    private ConstraintLayout main_navigation_search_image_container = null;
    private ConstraintLayout main_navigation_library = null;
    private ConstraintLayout main_navigation_library_image_container = null;

    private Fragment_Homescreen frag_home = new Fragment_Homescreen();
    private Fragment_Search frag_search = new Fragment_Search();
    private Fragment_Library frag_library = new Fragment_Library();

    private RelativeLayout mainRelativeLayout = null;
    private ConstraintLayout searchRelativeLayout = null;
    private PlayerBroadcastReceiver receiver = null;
    private PlaybackStopReceiver playbackStopReceiver = null;
    private UpdateReceiver updateReceiver = null;
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

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateChecker.install(MainActivity.this);
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        UpdateChecker.initiateCompleteFlow(MainActivity.this);

        BackStack.clear();

        root_intent = getIntent();

        setup_initial_screen_data();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        MusicForegroundService.NowPlayingData.getInstance(this);

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
//        navigationView = findViewById(R.id.bottom_nav_bar);

        setupNavigation();

        main_activity_parent = findViewById(R.id.main_activity_parent);

        SnackBar.setMainActivityView(findViewById(R.id.snackbar_view));

        mini_player = findViewById(R.id.mini_player);
        miniplayer_albumart = findViewById(R.id.miniplayer_albumart);
        miniplayer_title = findViewById(R.id.miniplayer_title);
        miniplayer_artist = findViewById(R.id.miniplayer_artist);

        mini_player.setOnClickListener(view -> {
            Intent intent = new Intent(this, NowPlayingNew.class);
            launcher.launch(intent);
        });

        mini_player_play_pause_ripple_container = findViewById(R.id.mini_player_play_pause_ripple_container);
        mini_player_play_pause_container = findViewById(R.id.mini_player_play_pause_container);

        mini_player_loader = findViewById(R.id.mini_player_loader);
        mini_player_loader.setOnClickListener(v -> {});

        mini_player_playbutton = findViewById(R.id.mini_player_playbutton);
        mini_player_pausebutton = findViewById(R.id.mini_player_pausebutton);

        mini_player_play_pause_container.setOnClickListener(v -> {
            Common.vibrate(this, 50);
            if (music_service == null) return;
            if (mini_player_playbutton.getVisibility() == View.VISIBLE) {
                if (!music_service.isPlaying()) music_service.play_manual();
                mini_player_playbutton.setVisibility(View.GONE);
                mini_player_pausebutton.setVisibility(View.VISIBLE);
                return;
            }
            if (mini_player_pausebutton.getVisibility() == View.VISIBLE) {
                if (music_service.isPlaying()) music_service.pause_manual();
                mini_player_pausebutton.setVisibility(View.GONE);
                mini_player_playbutton.setVisibility(View.VISIBLE);
                return;
            }
        });

        mini_player_progressbar = findViewById(R.id.mini_player_progressbar);

        setHome();

//        navigationView.setOnItemSelectedListener(item -> {
//            Fragment frag = getFragment(item.getItemId());
//            String tag = getFragmentTag(item.getItemId());
//            if (frag == null) return false;
//            setFragment(frag, tag);
//            return true;
//        });
//        navigationView.getMenu().getItem(0).setChecked(true);

        if (
                MusicService.isForegroundServiceRunning(this) &&
                MusicForegroundService.NowPlayingData.getInstance(this).getQueueTrackList() != null &&
                MusicForegroundService.NowPlayingData.getInstance(this).getQueueTrackList().size() > 0
        ) {
            handleMiniPlayer();
        }

        initialize();

//        new Handler().postDelayed(() -> {
//            startActivity(new Intent(this, LoginActivity.class));
//            finishAffinity();
//        }, 2000);

    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        root_intent = intent;
        if (root_intent == null) return;
        setup_initial_screen_data();
        setupNavigation();
        if (FragmentTags.HOMESCREEN.equals(CURRENT_SCREEN)) {
            setFragment(frag_home, FragmentTags.HOMESCREEN);
            if (albumview_albumid != null) {
                openAlbumViewFragment(albumview_albumid);
            }
        }
        else if (FragmentTags.SEARCH.equals(CURRENT_SCREEN)) setFragment(frag_search, FragmentTags.SEARCH);
        else if (FragmentTags.LIBRARY.equals(CURRENT_SCREEN)) setFragment(frag_library, FragmentTags.LIBRARY);
        checkNowPlayingIntent();
    };

    private void setup_initial_screen_data() {

        CURRENT_SCREEN = FragmentTags.HOMESCREEN;

        albumview_albumid = null;
        albumview_is_playable = false;
        albumview_trackid = null;

        if (root_intent == null) return;
        if (!Intent.ACTION_VIEW.equals(root_intent.getAction())) return;

        Uri uri = root_intent.getData();

        if (uri == null) return;

        List<String> segments = uri.getPathSegments();

        if (!segments.contains("player")) return;

        if (segments.contains("homescreen")) {
            CURRENT_SCREEN = FragmentTags.HOMESCREEN;
            return;
        }

        if (segments.contains("library")) {
            CURRENT_SCREEN = FragmentTags.LIBRARY;
            return;
        }

        if (segments.contains("search")) {

            Set<String> query_names = uri.getQueryParameterNames();

            if (query_names == null) return;
            if (!query_names.contains("q")) return;

            CURRENT_SCREEN = FragmentTags.SEARCH;
            String query = uri.getQueryParameter("q");

            if (!BackStack.hasRootFragment(FragmentTags.SEARCH)) {
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                frag_search.setArguments(bundle);
            }
            else {
                frag_search.setQuery(query);
            }

            return;

        }

        if (segments.contains("album")) {

            if (segments.size() < 3 || segments.size() > 4) return;

            String albumId = segments.get(2);
            if (albumId == null) return;

            CURRENT_SCREEN = FragmentTags.HOMESCREEN;
            albumview_albumid = albumId;
            albumview_is_playable = segments.contains("playable");
            albumview_trackid = null;

            return;

        }

        if (segments.contains("track")) {

            if (segments.size() < 4 || segments.size() > 5) return;

            String albumId = segments.get(2);
            if (albumId == null) return;

            String trackId = segments.get(3);
            if (trackId == null) return;

            CURRENT_SCREEN = FragmentTags.HOMESCREEN;
            albumview_albumid = albumId;
            albumview_is_playable = segments.contains("playable");
            albumview_trackid = trackId;

            return;

        }

    };

    private void setupNavigation() {

        main_navigation_home = findViewById(R.id.main_navigation_home);
        main_navigation_home_image_container = findViewById(R.id.main_navigation_home_image_container);
        main_navigation_search = findViewById(R.id.main_navigation_search);
        main_navigation_search_image_container = findViewById(R.id.main_navigation_search_image_container);
        main_navigation_library = findViewById(R.id.main_navigation_library);
        main_navigation_library_image_container = findViewById(R.id.main_navigation_library_image_container);

        GradientDrawable drawable = null;

        drawable = (GradientDrawable) main_navigation_home_image_container.getBackground();
        if (FragmentTags.HOMESCREEN.equals(CURRENT_SCREEN)) drawable.setColor(getColor(NAVIGATION_SELECTED_COLOR));
        else drawable.setColor(getColor(NAVIGATION_NON_SELECTED_COLOR));
        main_navigation_home_image_container.setBackground(drawable);

        drawable = (GradientDrawable) main_navigation_search_image_container.getBackground();
        if (FragmentTags.SEARCH.equals(CURRENT_SCREEN)) drawable.setColor(getColor(NAVIGATION_SELECTED_COLOR));
        else drawable.setColor(getColor(NAVIGATION_NON_SELECTED_COLOR));
        main_navigation_search_image_container.setBackground(drawable);

        drawable = (GradientDrawable) main_navigation_library_image_container.getBackground();
        if (FragmentTags.LIBRARY.equals(CURRENT_SCREEN)) drawable.setColor(getColor(NAVIGATION_SELECTED_COLOR));
        else drawable.setColor(getColor(NAVIGATION_NON_SELECTED_COLOR));
        main_navigation_library_image_container.setBackground(drawable);

        main_navigation_home_image_container.setOnClickListener(v -> {
            View parent = (View) v.getParent();
            if (parent == null) return;
            parent.performClick();
        });

        main_navigation_home.setOnClickListener(v -> {

            if (CURRENT_SCREEN == null) return;
            if (CURRENT_SCREEN.equals(FragmentTags.HOMESCREEN)) {
                setFragment(frag_home, FragmentTags.HOMESCREEN);
                return;
            }

            ConstraintLayout previous_screen = getSpecificScreen(CURRENT_SCREEN);
            ConstraintLayout new_screen = getSpecificScreen(FragmentTags.HOMESCREEN);

            GradientDrawable bg_drawable = null;

            bg_drawable = (GradientDrawable) previous_screen.getBackground();
            bg_drawable.setColor(getColor(NAVIGATION_NON_SELECTED_COLOR));
            previous_screen.setBackground(bg_drawable);

            bg_drawable = (GradientDrawable) new_screen.getBackground();
            bg_drawable.setColor(getColor(NAVIGATION_SELECTED_COLOR));
            new_screen.setBackground(bg_drawable);

            CURRENT_SCREEN = FragmentTags.HOMESCREEN;

            setFragment(frag_home, FragmentTags.HOMESCREEN);

        });

        main_navigation_search_image_container.setOnClickListener(v -> {
            View parent = (View) v.getParent();
            if (parent == null) return;
            parent.performClick();
        });

        main_navigation_search.setOnClickListener(v -> {

            if (CURRENT_SCREEN == null) return;
            if (CURRENT_SCREEN.equals(FragmentTags.SEARCH)) {
                setFragment(frag_search, FragmentTags.SEARCH);
                return;
            }

            ConstraintLayout previous_screen = getSpecificScreen(CURRENT_SCREEN);
            ConstraintLayout new_screen = getSpecificScreen(FragmentTags.SEARCH);

            GradientDrawable bg_drawable = null;

            bg_drawable = (GradientDrawable) previous_screen.getBackground();
            bg_drawable.setColor(getColor(NAVIGATION_NON_SELECTED_COLOR));
            previous_screen.setBackground(bg_drawable);

            bg_drawable = (GradientDrawable) new_screen.getBackground();
            bg_drawable.setColor(getColor(NAVIGATION_SELECTED_COLOR));
            new_screen.setBackground(bg_drawable);

            CURRENT_SCREEN = FragmentTags.SEARCH;

            setFragment(frag_search, FragmentTags.SEARCH);

        });

        main_navigation_library_image_container.setOnClickListener(v -> {
            View parent = (View) v.getParent();
            if (parent == null) return;
            parent.performClick();
        });

        main_navigation_library.setOnClickListener(v -> {

            if (CURRENT_SCREEN == null) return;
            if (CURRENT_SCREEN.equals(FragmentTags.LIBRARY)) {
                setFragment(frag_library, FragmentTags.LIBRARY);
                return;
            }

            ConstraintLayout previous_screen = getSpecificScreen(CURRENT_SCREEN);
            ConstraintLayout new_screen = getSpecificScreen(FragmentTags.LIBRARY);

            GradientDrawable bg_drawable = null;

            bg_drawable = (GradientDrawable) previous_screen.getBackground();
            bg_drawable.setColor(getColor(NAVIGATION_NON_SELECTED_COLOR));
            previous_screen.setBackground(bg_drawable);

            bg_drawable = (GradientDrawable) new_screen.getBackground();
            bg_drawable.setColor(getColor(NAVIGATION_SELECTED_COLOR));
            new_screen.setBackground(bg_drawable);

            CURRENT_SCREEN = FragmentTags.LIBRARY;

            setFragment(frag_library, FragmentTags.LIBRARY);

        });

    };

    private ConstraintLayout getSpecificScreen(String frag_tag) {
        if (frag_tag.equals(FragmentTags.HOMESCREEN)) return main_navigation_home_image_container;
        if (frag_tag.equals(FragmentTags.SEARCH)) return main_navigation_search_image_container;
        if (frag_tag.equals(FragmentTags.LIBRARY)) return main_navigation_library_image_container;
        return null;
    };

    private void setHome() {

        Fragment right_frag = null;
        String right_tag = null;

        if (FragmentTags.HOMESCREEN.equals(CURRENT_SCREEN)) {
            right_frag = frag_home;
            right_tag = FragmentTags.HOMESCREEN;
        }
        else if (FragmentTags.SEARCH.equals(CURRENT_SCREEN)) {
            right_frag = frag_search;
            right_tag = FragmentTags.SEARCH;
        }
        else if (FragmentTags.LIBRARY.equals(CURRENT_SCREEN)) {
            right_frag = frag_library;
            right_tag = FragmentTags.LIBRARY;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_frame_layout, right_frag, right_tag)
                .commit();

        BackStack.addFragment(right_frag);
        BackStack.addFragmentAsRoot(right_frag);

        boolean logged_in = GoogleSignIn.getLastSignedInAccount(this) != null;
        if (albumview_albumid != null && logged_in) {
            openAlbumViewFragment(albumview_albumid);
        }

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

        MusicService.bind(this, serviceConnection);

        runProgressHandler();

        checkNowPlayingIntent();

    };

    private void checkNowPlayingIntent() {

        if (root_intent == null) return;

        boolean openNowPlaying = root_intent.getBooleanExtra("open_now_playing", false);
        if (!openNowPlaying) return;

        if (mini_player == null) return;

        mini_player.performClick();

    };

    private void runProgressHandler() {

        progressHandler = new Handler();

        final int TIME = 200;

        progressHandlerRunnable = new Runnable() {
            @Override
            public void run() {

                MusicForegroundService.NowPlayingData currentTrack = MusicForegroundService.NowPlayingData.getInstance(getApplicationContext());

                // initial play/pause button set
                if (!initialPlayPauseButtonSet && currentTrack.getMediaPrepared()) {
                    mini_player_loader.setVisibility(View.GONE);
                    mini_player_play_pause_ripple_container.setVisibility(View.VISIBLE);
                    mini_player_playbutton.setVisibility(View.GONE);
                    mini_player_pausebutton.setVisibility(View.VISIBLE);
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

                progressHandler.postDelayed(this, TIME);

            }
        };

        progressHandler.postDelayed(progressHandlerRunnable, TIME);

    };

    private Fragment getFragment(int id) {
        if (id == R.id.homescreen) return frag_home;
        if (id == R.id.search) return frag_search;
        if (id == R.id.library) return frag_library;
        return null;
    };

    private String getFragmentTag(int id) {
        if (id == R.id.homescreen) return FragmentTags.HOMESCREEN;
        if (id == R.id.search) return FragmentTags.SEARCH;
        if (id == R.id.library) return FragmentTags.LIBRARY;
        return null;
    };

    private void setFragment(Fragment fragment, String tag) {

        if (!BackStack.hasSomeRootFragment()) {
            setHome();
            return;
        }

        boolean hadMoreThanRoot = BackStack.clearTillRoot(this);

        if (BackStack.hasRootFragment(tag)) {

            Fragment top_fragment = BackStack.getTopFragment();

            if (tag.equals(top_fragment.getTag()) && !hadMoreThanRoot) return;

            BackStack.removeTopFragment();

            getSupportFragmentManager().beginTransaction().hide(top_fragment).commit();
            getSupportFragmentManager().beginTransaction().show(fragment).commit();

            BackStack.addFragment(fragment);

        }
        else {

            Fragment top_fragment = BackStack.removeTopFragment();

            getSupportFragmentManager().beginTransaction().hide(top_fragment).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, fragment, tag).commit();
            getSupportFragmentManager().beginTransaction().show(fragment).commit();

            BackStack.addFragment(fragment);
            BackStack.addFragmentAsRoot(fragment);

        }

//        Fragment_AlbumView fragment_albumView = (Fragment_AlbumView) getSupportFragmentManager().findFragmentByTag(albumview_fragment_tag);
//        if (fragment_albumView != null) {
//            getSupportFragmentManager().beginTransaction().remove(fragment_albumView).commit();
//        }
//
//        previous_fragment = current_fragment;
//
//        Fragment existing = (Fragment) getSupportFragmentManager().findFragmentByTag(tag);
//        if (existing == null) {
//            getSupportFragmentManager().beginTransaction().hide(current_fragment).commit();
//            getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, fragment, tag).commit();
//            getSupportFragmentManager().beginTransaction().show(fragment).commit();
//
//        }
//        else {
//            getSupportFragmentManager().beginTransaction().hide(current_fragment).commit();
//            getSupportFragmentManager().beginTransaction().show(fragment).commit();
//        }
//
//        current_fragment = fragment;

//        getSupportFragmentManager()
//            .beginTransaction()
//            .replace(R.id.main_frame_layout, fragment, tag)
//            .commit();

    };

    public void openAlbumViewFragment(String albumId) {

        Fragment_AlbumView fragment = new Fragment_AlbumView();
        Bundle bundle = new Bundle();
        bundle.putString("album_id", albumId);
        if (albumview_is_playable) bundle.putBoolean("should_play", true);
        if (albumview_trackid != null) bundle.putString("track_id", albumview_trackid);
        fragment.setArguments(bundle);

        Fragment top_fragment = BackStack.getTopFragment();

        if (FragmentTags.ALBUMVIEW.equals(top_fragment.getTag())) {
            getSupportFragmentManager().beginTransaction().remove(top_fragment).commit();
            BackStack.removeTopFragment();
        }

        top_fragment = BackStack.getTopFragment();

        getSupportFragmentManager().beginTransaction().hide(top_fragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, fragment, FragmentTags.ALBUMVIEW).commit();
        getSupportFragmentManager().beginTransaction().show(fragment).commit();

        BackStack.addFragment(fragment);

//        previous_fragment = current_fragment;
//
//        getSupportFragmentManager().beginTransaction().hide(current_fragment).commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, fragment, albumview_fragment_tag).commit();
//        getSupportFragmentManager().beginTransaction().show(fragment).commit();
//
//        current_fragment = fragment;

    };

    public void openMoreSearchFragment(String json_string, MusicApplication.MORE_SEARCH_TYPES type) {

        Fragment_More_Search fragment = new Fragment_More_Search();
        Bundle bundle = new Bundle();
        bundle.putString("data", json_string);
        bundle.putString("type", type.toString());
        fragment.setArguments(bundle);

        Fragment top_fragment = BackStack.getTopFragment();

//        if (FragmentTags.SEARCH.equals(top_fragment.getTag())) {
//            getSupportFragmentManager().beginTransaction().remove(top_fragment).commit();
//            BackStack.removeTopFragment();
//        }
//
//        top_fragment = BackStack.removeTopFragment();

        getSupportFragmentManager().beginTransaction().hide(top_fragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, fragment, FragmentTags.MORE_SEARCH).commit();
        getSupportFragmentManager().beginTransaction().show(fragment).commit();

        BackStack.addFragment(fragment);

//        previous_fragment = current_fragment;
//
//        getSupportFragmentManager().beginTransaction().hide(current_fragment).commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, fragment, more_search_fragment_tag).commit();
//        getSupportFragmentManager().beginTransaction().show(fragment).commit();
//
//        current_fragment = fragment;

    };

    public void backPressFromAlbumView() {

        Fragment top_fragment = BackStack.removeTopFragment();
        Fragment next_fragment = BackStack.getTopFragment();

        getSupportFragmentManager().beginTransaction().hide(top_fragment).commit();
        getSupportFragmentManager().beginTransaction().show(next_fragment).commit();
        getSupportFragmentManager().beginTransaction().remove(top_fragment).commit();

//        getSupportFragmentManager().beginTransaction().hide(current_fragment).commit();
//        getSupportFragmentManager().beginTransaction().show(previous_fragment).commit();
//        getSupportFragmentManager().beginTransaction().remove(current_fragment).commit();
//
//        current_fragment = previous_fragment;
//        previous_fragment = null;

    };

    public void backPressFromMoreSearchView() {

        Fragment top_fragment = BackStack.removeTopFragment();
        Fragment next_fragment = BackStack.getTopFragment();

        getSupportFragmentManager().beginTransaction().hide(top_fragment).commit();
        getSupportFragmentManager().beginTransaction().show(next_fragment).commit();
        getSupportFragmentManager().beginTransaction().remove(top_fragment).commit();

//        getSupportFragmentManager().beginTransaction().hide(current_fragment).commit();
//        getSupportFragmentManager().beginTransaction().show(previous_fragment).commit();
//        getSupportFragmentManager().beginTransaction().remove(current_fragment).commit();
//
//        current_fragment = previous_fragment;
//        previous_fragment = null;

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
        Track now_playing = MusicForegroundService.NowPlayingData.getInstance(this).getTrack();
        Glide.with(this).asBitmap().load(now_playing.getThumbnail()).into(miniplayer_albumart);
        miniplayer_title.setText(now_playing.getTitle());
        miniplayer_artist.setText(now_playing.getArtist());
        mini_player_progressbar.setMax(0);
        mini_player_progressbar.setProgress(0);
        mini_player_progressbar_max_set = false;
        initialPlayPauseButtonSet = false;
        mini_player_loader.setVisibility(View.VISIBLE);
        mini_player_play_pause_ripple_container.setVisibility(View.GONE);
        mini_player_playbutton.setVisibility(View.GONE);
        mini_player_pausebutton.setVisibility(View.GONE);
        if (mini_player.getVisibility() == View.VISIBLE) return;
        showMiniPlayer();
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        Fragment_Search search_fragment = (Fragment_Search) getSupportFragmentManager().findFragmentByTag(FragmentTags.SEARCH);
        if (search_fragment == null) return super.dispatchTouchEvent(event);

        View fragment_view = search_fragment.getView();
        if (fragment_view == null) return super.dispatchTouchEvent(event);

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
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        super.onPause();
    };

    @Override
    protected void onPostResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicApplication.UPDATE_DOWNLOAD);
        if (updateReceiver == null) updateReceiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter);
        super.onPostResume();
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
        MusicService.unBind(this, serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackStopReceiver);
    };

};
