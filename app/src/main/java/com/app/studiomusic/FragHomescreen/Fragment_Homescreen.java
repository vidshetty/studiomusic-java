package com.app.studiomusic.FragHomescreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.app.studiomusic.MessageTransfer.Function;
import com.app.studiomusic.MessageTransfer.MessageTransfer;
import com.bumptech.glide.Glide;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.ProfileImage;
import com.app.studiomusic.Common.SnackBar;
import com.app.studiomusic.Main.FragmentTags;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.Profile.ProfileActivity;
import com.app.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Fragment_Homescreen extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<Album> new_releases = null;
    private List<Album> recently_added = null;
    private List<MostPlayedItem> most_played = null;
    private List<Track> quick_picks = null;

    private Most_Played_Adapter most_played_adapter = null;
    private NewReleases_Adapter newReleases_adapter = null;
    private Quick_Picks_Adapter quick_picks_adapter = null;
    private RecentlyAdded_Adapter recentlyAdded_adapter = null;
    private PlayPauseBroadcastReceiver playPauseReceiver = null;
    private TrackChangeBroadcastReceiver trackChangeReceiver = null;

    private SwipeRefreshLayout swipeRefreshLayout = null;
    private CoordinatorLayout parent = null;
    private ImageView progileImage = null;
    private ConstraintLayout fullScreen_loader = null;
    private ConstraintLayout fullScreen_nonloader = null;
    private NestedScrollView content = null;
    private Toolbar main_toolbar = null;
    private ConstraintLayout mostplayed_container = null;
    private RecyclerView most_played_recyclerview = null;
    private RecyclerView quickpicks_recyclerview = null;
    private RecyclerView newreleases_recyclerview = null;
    private RecyclerView recentlyadded_recyclerview = null;

    private final QuickPicksTouch quickPicksTouch = new QuickPicksTouch() {
        @Override
        public void click(int position) {
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .setNowPlayingTrack(quick_picks.get(position));
//                .commit();
            MusicService.start(getContext());
            callRadio(quick_picks.get(position).getTrackId());
        };
        @Override
        public boolean longClick(int position) {
            return false;
        };
        @Override
        public void menuClick(int position, boolean should_vibrate) {
            if (should_vibrate) Common.vibrate(getContext(), 50);
            openTrackMenu(quick_picks, position);
        };
    };

    private final NewReleasesTouch newReleasesTouch = new NewReleasesTouch() {
        @Override
        public void click(int position) {
            openAlbumViewFragment(new_releases.get(position).getAlbumId());
        };
        @Override
        public boolean longClick(int position) {
            openAlbumMenu(new_releases, position, false);
            return true;
        };
    };

    private final RecentlyAddedTouch recentlyAddedTouch = new RecentlyAddedTouch() {
        @Override
        public void click(int position) {
            openAlbumViewFragment(recently_added.get(position).getAlbumId());
        };
        @Override
        public boolean longClick(int position) {
            openAlbumMenu(recently_added, position, false);
            return true;
        };
    };

    private final MostPlayedTouch mostPlayedTouch = new MostPlayedTouch() {
        @Override
        public void click(int position) {
            playFromMostPlayed(position);
        };
        @Override
        public boolean longClick(int position) {
            List<Album> album_list = new ArrayList<>();
            for (int i=0; i<most_played.size(); i++) album_list.add(most_played.get(i).getAlbum());
            openAlbumMenu(album_list, position, true);
            return true;
        };
    };

    private final Function callback = (bundle) -> {
        if (bundle == null) return;
        Log.d("callback_trial", bundle.getString("random") + " ");
    };

    private final class PlayPauseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            playPauseChangeHandler();
        };
    };

    private final class TrackChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) { trackChangeHandler(); };
    };

    public Fragment_Homescreen() {
        // Required empty public constructor
    };

    public static Fragment_Homescreen newInstance(String param1, String param2) {
        Fragment_Homescreen fragment = new Fragment_Homescreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment__homescreen, container, false);

        MessageTransfer.register(FragmentTags.HOMESCREEN, callback);

        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.black));

        parent = view.findViewById(R.id.homescreen_parent);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) parent.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getContext());
        parent.setLayoutParams(params);

        main_toolbar = view.findViewById(R.id.main_toolbar);

        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new_releases = new ArrayList<>();
                quick_picks = new ArrayList<>();
                recently_added = new ArrayList<>();
                most_played = new ArrayList<>();
                APIService.getHomeAlbums(getContext(), Fragment_Homescreen.this::response, Fragment_Homescreen.this::responseError);
            };
        });

        progileImage = view.findViewById(R.id.profileImage);

        fullScreen_loader = view.findViewById(R.id.homescreen_fragment_loader);

        fullScreen_nonloader = view.findViewById(R.id.homescreen_fragment_nonloader);

        mostplayed_container = view.findViewById(R.id.mostPlayed_container);
        most_played_recyclerview = view.findViewById(R.id.mostPlayed_recyclerview);
//        most_played_recyclerview.setHasFixedSize(true);
        LinearLayoutManager mostplayed_layout = new LinearLayoutManager(getContext());
        mostplayed_layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        most_played_recyclerview.setLayoutManager(mostplayed_layout);

        quickpicks_recyclerview = view.findViewById(R.id.quickpicks_recyclerview);
        quickpicks_recyclerview.setHasFixedSize(true);
        GridLayoutManager quickpicks_layout = new GridLayoutManager(getContext(), 4, GridLayoutManager.HORIZONTAL, false);
        quickpicks_recyclerview.setLayoutManager(quickpicks_layout);
        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(quickpicks_recyclerview);

        newreleases_recyclerview = view.findViewById(R.id.newreleases_recyclerview);
        newreleases_recyclerview.setHasFixedSize(true);
        LinearLayoutManager newreleases_layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        newreleases_recyclerview.setLayoutManager(newreleases_layout);

        recentlyadded_recyclerview = view.findViewById(R.id.recentlyadded_recyclerview);
        recentlyadded_recyclerview.setHasFixedSize(true);
        LinearLayoutManager recentlyadded_layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recentlyadded_recyclerview.setLayoutManager(recentlyadded_layout);

        if (playPauseReceiver == null) {
            playPauseReceiver = new PlayPauseBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(MusicApplication.PLAY_PAUSE);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(playPauseReceiver, intentFilter);
        }

        if (trackChangeReceiver == null) {
            trackChangeReceiver = new TrackChangeBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_CHANGE);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(trackChangeReceiver, intentFilter);
        }

        if (
                new_releases != null && quick_picks != null &&
                recently_added != null && most_played != null
        ) {

            fullScreen_loader.setVisibility(View.GONE);
            fullScreen_nonloader.setVisibility(View.VISIBLE);

//            mostplayed_container.setVisibility(View.GONE);
            if (most_played.size() > 0) {
                most_played_adapter = new Most_Played_Adapter(getContext(), most_played, mostPlayedTouch);
                most_played_recyclerview.setAdapter(most_played_adapter);
            }
            else mostplayed_container.setVisibility(View.GONE);

            quick_picks_adapter = new Quick_Picks_Adapter(getContext(), quick_picks, quickPicksTouch);
            quickpicks_recyclerview.setAdapter(quick_picks_adapter);

            newReleases_adapter = new NewReleases_Adapter(getContext(), new_releases, newReleasesTouch);
            newreleases_recyclerview.setAdapter(newReleases_adapter);

            recentlyAdded_adapter = new RecentlyAdded_Adapter(getContext(), recently_added, recentlyAddedTouch);
            recentlyadded_recyclerview.setAdapter(recentlyAdded_adapter);

            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(fullScreen_nonloader);
                TransitionManager.beginDelayedTransition(parent, transition);
                fullScreen_nonloader.setVisibility(View.GONE);
            }, 0);

        }
        else {

            fullScreen_loader.setVisibility(View.VISIBLE);
            fullScreen_nonloader.setVisibility(View.GONE);

            new_releases = new ArrayList<>();
            quick_picks = new ArrayList<>();
            recently_added = new ArrayList<>();
            most_played = new ArrayList<>();

            APIService.getHomeAlbums(getContext(), this::response, this::responseError);

        }

        setProfileImage();

        return view;

    };

    private void setProfileImage() {

        progileImage.setOnClickListener(view -> {
            Common.vibrate(getContext(), 50);
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (account == null) return;

        Glide.with(this)
                .load(account.getPhotoUrl() == null ? ProfileImage.getAlternativePicture(account.getDisplayName()) : account.getPhotoUrl())
                .into(progileImage);

    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(playPauseReceiver);
        playPauseReceiver = null;
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(trackChangeReceiver);
        trackChangeReceiver = null;
    };

    private void playFromMostPlayed(int position) {
        Album album = most_played.get(position).getAlbum();
        List<Track> tracks = album.getTracks();
        MusicForegroundService.NowPlayingData.getInstance(getContext()).setNowPlayingTrack(tracks.get(0));
        MusicService.start(getContext());
        for (int i=1; i<tracks.size(); i++) {
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                    .addToQueue(tracks.get(i));
        }
        callMostPlayedRadio(album.getAlbumId());
    };

    private void openAlbumViewFragment(String albumId) {
        ((MainActivity) getActivity()).openAlbumViewFragment(albumId);
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            fullScreen_nonloader.setVisibility(View.VISIBLE);
        }
        else {

            fullScreen_nonloader.setVisibility(View.VISIBLE);
            fullScreen_loader.setVisibility(View.GONE);

            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(fullScreen_nonloader);
                TransitionManager.beginDelayedTransition(parent, transition);
                fullScreen_nonloader.setVisibility(View.GONE);
            }, 0);

        }
    };

    private void response(JSONObject response) {

        try {

            JSONObject albums = response.getJSONObject("albums");

            JSONArray newReleases = albums.getJSONArray("New Releases");
            JSONArray recentlyAdded = albums.getJSONArray("Recently Added");
            JSONArray mostPlayed = response.getJSONArray("mostPlayed");
            JSONArray quickPicks = response.getJSONArray("quickPicks");

            for (int i=0; i<newReleases.length(); i++) {
                new_releases.add(Common.buildAlbum(newReleases.getJSONObject(i)));
            }

            for (int i=0; i<recentlyAdded.length(); i++) {
                recently_added.add(Common.buildAlbum(recentlyAdded.getJSONObject(i)));
            }

            for (int i=0; i<mostPlayed.length(); i++) {
                Album album = Common.buildAlbum(mostPlayed.getJSONObject(i));
                Track now_playing = MusicForegroundService.NowPlayingData.getInstance(getContext()).getTrack();
                MostPlayedItem item = new MostPlayedItem(album);
                if (now_playing == null || !now_playing.getAlbumId().equals(album.getAlbumId())) {
                    item.setNowPlaying(false);
                    item.setPaused(true);
                }
                else {
                    item.setNowPlaying(true);
                    boolean isPlaying = MusicForegroundService.NowPlayingData.getInstance(getContext()).getMediaIsPlaying();
                    item.setPaused(!isPlaying);
                }
                most_played.add(item);
//                most_played.add(Common.buildAlbum(mostPlayed.getJSONObject(i)));
            }

            for (int i=0; i<quickPicks.length(); i++) {
                JSONObject each = quickPicks.getJSONObject(i);
                quick_picks.add(Common.buildTrack(each));
            }

//            mostplayed_container.setVisibility(View.GONE);
            if (most_played.size() > 0) {
                most_played_adapter = new Most_Played_Adapter(getContext(), most_played, mostPlayedTouch);
                most_played_recyclerview.setAdapter(most_played_adapter);
                mostplayed_container.setVisibility(View.VISIBLE);
            }
            else mostplayed_container.setVisibility(View.GONE);

            quick_picks_adapter = new Quick_Picks_Adapter(getContext(), quick_picks, quickPicksTouch);
            quickpicks_recyclerview.setAdapter(quick_picks_adapter);

            newReleases_adapter = new NewReleases_Adapter(getContext(), new_releases, newReleasesTouch);
            newreleases_recyclerview.setAdapter(newReleases_adapter);

            recentlyAdded_adapter = new RecentlyAdded_Adapter(getContext(), recently_added, recentlyAddedTouch);
            recentlyadded_recyclerview.setAdapter(recentlyAdded_adapter);

            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(fullScreen_loader);
                TransitionManager.beginDelayedTransition(parent, transition);
                fullScreen_loader.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }, 0);

        }
        catch(JSONException e) {
            e.printStackTrace();
            return;
        }

    };

    private void responseError(VolleyError err) {
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getActivity().getApplicationContext(), "Error fetching home feed!", Toast.LENGTH_SHORT).show();
    };

    private void openAlbumMenu(List<Album> list, int position, boolean show_remove) {

        Album album = list.get(position);

        View.OnClickListener goToHome = view -> {
//            Common.vibrate(getContext(), 100);
            openAlbumViewFragment(album.getAlbumId());
        };

        View.OnClickListener share = view -> {
//            Common.vibrate(getContext(), 100);
            Intent shareIntent = Common.shareAlbum(album);
            if (shareIntent == null) return;
            startActivity(shareIntent);
        };

        View.OnClickListener playNext = view -> {
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Playing " + album.getAlbum() + " next", 2000);
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .playNextMultipleTracks(album.getTracks());
//            Toast.makeText(getContext(), "Playing " + album.getAlbum() + " next", Toast.LENGTH_SHORT).show();
        };

        View.OnClickListener addToQueue = view -> {
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Added " + album.getAlbum() + " to queue", 2000);
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .addToQueueMultipleTracks(album.getTracks());
//            Toast.makeText(getContext(), "Added " + album.getAlbum() + " to queue", Toast.LENGTH_SHORT).show();
        };

        View.OnClickListener remove = view -> {

            most_played.remove(position);
            most_played_adapter.notifyItemRemoved(position);

            if (most_played.size() == 0) mostplayed_container.setVisibility(View.GONE);

            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Removed " + album.getAlbum() + " from history", 2000);

            try {
                JSONObject object = new JSONObject();
                object.put("albumId", album.getAlbumId());
                APIService.removeFromRecentlyPlayed(getContext(), object);
            } catch (JSONException e) {
                return;
            }

        };

        List<BottomMenu.MenuItem> menu_list = new ArrayList<>();
        menu_list.add(new BottomMenu.MenuItem(null, "Go To Album", goToHome));
        menu_list.add(new BottomMenu.MenuItem(null, "Play Next", playNext));
        menu_list.add(new BottomMenu.MenuItem(null, "Add To Queue", addToQueue));
        menu_list.add(new BottomMenu.MenuItem(null, "Share Album", share));

        if (show_remove) {
            menu_list.add(new BottomMenu.MenuItem(null, "Remove From History", remove));
        }

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(getContext())
                .setDataList(menu_list)
                .setName(album.getAlbum())
                .setArtist(album.getAlbumArtist())
                .setThumbnail(album.getThumbnail())
                .build();

        bottomMenu_new.show();

    };

    private void openTrackMenu(List<Track> list, int position) {

        Track track = list.get(position);

        View.OnClickListener goToHome = view -> {
//            Common.vibrate(getContext(), 100);
            openAlbumViewFragment(track.getAlbumId());
        };

        View.OnClickListener share = view -> {
//            Common.vibrate(getContext(), 100);
            Intent shareIntent = Common.shareTrack(track);
            if (shareIntent == null) return;
            startActivity(shareIntent);
        };

        View.OnClickListener playNext = view -> {
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Playing " + track.getTitle() + " next", 2000);
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .playNext(track);
//            Toast.makeText(getContext(), "Playing " + track.getTitle() + " next", Toast.LENGTH_SHORT).show();
        };

        View.OnClickListener addToQueue = view -> {
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Added " + track.getTitle() + " to queue", 2000);
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .addToQueue(track);
//            Toast.makeText(getContext(), "Added " + track.getTitle() + " to queue", Toast.LENGTH_SHORT).show();
        };

        List<BottomMenu.MenuItem> menu_list = Arrays.asList(
                new BottomMenu.MenuItem(null, "Go To Album", goToHome),
                new BottomMenu.MenuItem(null, "Play Next", playNext),
                new BottomMenu.MenuItem(null, "Add To Queue", addToQueue),
                new BottomMenu.MenuItem(null, "Share Track", share)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(getContext())
                .setDataList(menu_list)
                .setName(track.getTitle())
                .setArtist(track.getArtist())
                .setThumbnail(track.getThumbnail())
                .build();

        bottomMenu_new.show();

    };

    private void radioResponse(JSONArray response) {
        if (response == null) return;
        List<Track> tracks = new ArrayList<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject track_data = response.getJSONObject(i);
                Track track = Common.buildTrack(track_data);
                tracks.add(track);
            }
            MusicForegroundService.NowPlayingData.getInstance(getContext()).addToQueueMultipleTracks(tracks);
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Starting radio!", 2000);
            Intent intent = new Intent(MusicApplication.RADIO_RESPONSE);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
//            Toast.makeText(getContext(), "Starting radio!", Toast.LENGTH_SHORT).show();
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    };

    private void radioResponseError(VolleyError err) {
        Toast.makeText(getContext(), "Error while starting radio!", Toast.LENGTH_SHORT).show();
    };

    private void callRadio(String trackId) {
        JSONObject queryParams = new JSONObject();
        try {
            queryParams.put("trackId", trackId);
            APIService.startRadio(getContext(), queryParams, this::radioResponse, this::radioResponseError);
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    };

    private void callMostPlayedRadio(String albumId) {
        JSONObject queryParams = new JSONObject();
        try {
            queryParams.put("albumId", albumId);
            APIService.getMostPlayedRadio(getContext(), queryParams, this::radioResponse, this::radioResponseError);
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    };

    private void playPauseChangeHandler() {

        if (most_played_adapter == null) return;

        Track now_playing = MusicForegroundService.NowPlayingData.getInstance(getContext()).getTrack();
        if (now_playing == null) return;

        String trackAlbumId = now_playing.getAlbumId();

        int rightIndex = -1;
        for (int i=0; i<most_played.size(); i++) {
            if (most_played.get(i).getAlbum().getAlbumId().equals(trackAlbumId)) {
                rightIndex = i;
                break;
            }
        }

        if (rightIndex == -1) return;

        boolean isPlaying = MusicForegroundService.NowPlayingData.getInstance(getContext()).getMediaIsPlaying();
        most_played.get(rightIndex).setPaused(!isPlaying);

        most_played_adapter.notifyItemChanged(rightIndex);

    };

    private void trackChangeHandler() {

        if (most_played_adapter == null) return;

        int previous_playing_index = -1;
        for (int i=0; i<most_played.size(); i++) {
            if (most_played.get(i).getIsNowPlaying()) {
                previous_playing_index = i;
                break;
            }
        }

        if (previous_playing_index > -1) {
            most_played.get(previous_playing_index).setPaused(true);
            most_played.get(previous_playing_index).setNowPlaying(false);
            most_played_adapter.notifyItemChanged(previous_playing_index);
        }

        Track now_playing = MusicForegroundService.NowPlayingData.getInstance(getContext()).getTrack();
        if (now_playing == null) return;

        String trackAlbumId = now_playing.getAlbumId();

        int rightIndex = -1;
        for (int i=0; i<most_played.size(); i++) {
            if (most_played.get(i).getAlbum().getAlbumId().equals(trackAlbumId)) {
                rightIndex = i;
                break;
            }
        }

        if (rightIndex == -1) return;

        most_played.get(rightIndex).setNowPlaying(true);
        boolean isPlaying = MusicForegroundService.NowPlayingData.getInstance(getContext()).getMediaIsPlaying();
        most_played.get(rightIndex).setPaused(!isPlaying);

        most_played_adapter.notifyItemChanged(rightIndex);

    };

};