package com.example.studiomusic.FragHomescreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.Audio_Controller.MusicApplication;
import com.example.studiomusic.Audio_Controller.MusicForegroundService;
import com.example.studiomusic.Audio_Controller.MusicServiceNew;
import com.example.studiomusic.Audio_Controller.MusicForegroundService.NowPlayingData;
import com.example.studiomusic.BottomMenu.BottomMenu;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.Common.ProfileImage;
import com.example.studiomusic.CustomSwipeRefreshLayout.CustomSwipeRefreshLayout;
import com.example.studiomusic.FragAlbumView.Fragment_AlbumView;
import com.example.studiomusic.MusicData.Album;
import com.example.studiomusic.MusicData.Track;
import com.example.studiomusic.Profile.ProfileActivity;
import com.example.studiomusic.R;
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
    private List<Album> most_played = null;
    private List<Track> quick_picks = null;

    private Most_Played_Adapter most_played_adapter = null;
    private NewReleases_Adapter newReleases_adapter = null;
    private Quick_Picks_Adapter quick_picks_adapter = null;
    private RecentlyAdded_Adapter recentlyAdded_adapter = null;
    private PlayPauseBroadcastReceiver playPauseReceiver = null;

    private CustomSwipeRefreshLayout swipeRefreshLayout = null;
    private FrameLayout parent = null;
    private ImageView progileImage = null;
    private ConstraintLayout fullScreen = null;
    private ConstraintLayout fullScreen_nonloader = null;
    private ScrollView content = null;
    private ConstraintLayout mostplayed_container = null;
    private RecyclerView most_played_recyclerview = null;
    private RecyclerView quickpicks_recyclerview = null;
    private RecyclerView newreleases_recyclerview = null;
    private RecyclerView recentlyadded_recyclerview = null;

    private final QuickPicksTouch quickPicksTouch = new QuickPicksTouch() {
        @Override
        public void click(int position) {
            NowPlayingData.getInstance(getContext())
                .setNowPlayingTrack(quick_picks.get(position));
//                .commit();
            MusicServiceNew.start(getContext());
            callRadio(quick_picks.get(position).getTrackId());
        };
        @Override
        public boolean longClick(int position) {
            return false;
        };
        @Override
        public void menuClick(int position) {
            Common.vibrate(getContext(), 100);
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
            openAlbumMenu(new_releases, position);
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
            openAlbumMenu(recently_added, position);
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
            openAlbumMenu(most_played, position);
            return true;
        };
    };

    private final class PlayPauseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            playPauseChangeHandler();
        };
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

        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.black));

        parent = view.findViewById(R.id.homescreen_parent);

        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);

        swipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new_releases = new ArrayList<>();
                quick_picks = new ArrayList<>();
                recently_added = new ArrayList<>();
                most_played = new ArrayList<>();

                APIService.getHomeAlbums(getContext(), Fragment_Homescreen.this::response, Fragment_Homescreen.this::responseError);

            }
        });

        progileImage = view.findViewById(R.id.profileImage);

        fullScreen = view.findViewById(R.id.homescreen_fragment_loader);

        fullScreen_nonloader = view.findViewById(R.id.homescreen_fragment_nonloader);

        content = view.findViewById(R.id.homescreen_content);

        mostplayed_container = view.findViewById(R.id.mostPlayed_container);
        most_played_recyclerview = view.findViewById(R.id.mostPlayed_recyclerview);
        LinearLayoutManager mostplayed_layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        most_played_recyclerview.setLayoutManager(mostplayed_layout);

        quickpicks_recyclerview = view.findViewById(R.id.quickpicks_recyclerview);
        GridLayoutManager quickpicks_layout = new GridLayoutManager(getContext(), 4, GridLayoutManager.HORIZONTAL, false);
        quickpicks_recyclerview.setLayoutManager(quickpicks_layout);

        newreleases_recyclerview = view.findViewById(R.id.newreleases_recyclerview);
        LinearLayoutManager newreleases_layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        newreleases_recyclerview.setLayoutManager(newreleases_layout);

        recentlyadded_recyclerview = view.findViewById(R.id.recentlyadded_recyclerview);
        LinearLayoutManager recentlyadded_layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recentlyadded_recyclerview.setLayoutManager(recentlyadded_layout);

        if (playPauseReceiver == null) {
            playPauseReceiver = new PlayPauseBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(MusicApplication.PLAY_PAUSE);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(playPauseReceiver, intentFilter);
        }

        if (
                new_releases != null && quick_picks != null &&
                recently_added != null && most_played != null
        ) {

            fullScreen.setVisibility(View.GONE);
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

            fullScreen.setVisibility(View.VISIBLE);
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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        Glide.with(this)
                .load(account.getPhotoUrl() == null ? ProfileImage.getAlternativePicture(account.getDisplayName()) : account.getPhotoUrl())
                .into(progileImage);
        progileImage.setOnClickListener(view -> {
            Common.vibrate(getContext(), 50);
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(playPauseReceiver);
        playPauseReceiver = null;
    };

    private void playFromMostPlayed(int position) {
        Album album = most_played.get(position);
        List<Track> tracks = album.getTracks();
        NowPlayingData.getInstance(getContext()).setNowPlayingTrack(tracks.get(0));
        MusicServiceNew.start(getContext());
        for (int i=1; i<tracks.size(); i++) {
            NowPlayingData.getInstance(getContext())
                    .addToQueue(tracks.get(i));
        }
        callMostPlayedRadio(album.getAlbumId());
    };

    private void openAlbumViewFragment(String albumId) {

        Fragment_AlbumView fragment = new Fragment_AlbumView();
        Bundle bundle = new Bundle();
        bundle.putString("album_id", albumId);
        fragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame_layout, fragment, "ALBUMVIEW_FRAGMENT_TAG")
                .addToBackStack(null)
                .commit();

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
                most_played.add(Common.buildAlbum(mostPlayed.getJSONObject(i)));
            }

            for (int i=0; i<quickPicks.length(); i++) {
                JSONObject each = quickPicks.getJSONObject(i);
                quick_picks.add(Common.buildTrack(each));
            }

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
                transition.addTarget(fullScreen);
                TransitionManager.beginDelayedTransition(parent, transition);
                fullScreen.setVisibility(View.GONE);
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
        if (APIService.isAuthOrTimeError(err)) {
            APIService.errorHandler(getActivity().getApplicationContext(), err);
            return;
        }
        Toast.makeText(getActivity().getApplicationContext(), "Error fetching home feed!", Toast.LENGTH_SHORT).show();
    };

    private void openAlbumMenu(List<Album> list, int position) {

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
            NowPlayingData.getInstance(getContext())
                .playNextMultipleTracks(album.getTracks());
            Toast.makeText(getContext(), "Playing " + album.getAlbum() + " next", Toast.LENGTH_SHORT).show();
        };

        View.OnClickListener addToQueue = view -> {
            NowPlayingData.getInstance(getContext())
                .addToQueueMultipleTracks(album.getTracks());
            Toast.makeText(getContext(), "Added " + album.getAlbum() + " to queue", Toast.LENGTH_SHORT).show();
        };

        List<BottomMenu.MenuItem> menu_list = Arrays.asList(
                new BottomMenu.MenuItem(null, "Go To Album", goToHome),
                new BottomMenu.MenuItem(null, "Play Next", playNext),
                new BottomMenu.MenuItem(null, "Add To Queue", addToQueue),
                new BottomMenu.MenuItem(null, "Share Album", share)
        );

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
            NowPlayingData.getInstance(getContext())
                .playNext(track);
            Toast.makeText(getContext(), "Playing " + track.getTitle() + " next", Toast.LENGTH_SHORT).show();
        };

        View.OnClickListener addToQueue = view -> {
            NowPlayingData.getInstance(getContext())
                .addToQueue(track);
            Toast.makeText(getContext(), "Added " + track.getTitle() + " to queue", Toast.LENGTH_SHORT).show();
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
            NowPlayingData.getInstance(getContext()).addToQueueMultipleTracks(tracks);
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
        most_played_adapter.notifyDataSetChanged();

//        Track now_playing = NowPlayingData.getInstance(getContext()).getTrack();
//        if (now_playing == null) return;
//
//        String trackId = now_playing.getTrackId();
//        String albumId = now_playing.getAlbumId();
//
//        int most_played_index = -1;
//        for (int i=0; i<most_played.size(); i++) {
//            if (albumId.equals(most_played.get(i).getAlbumId())) {
//                most_played_index = i;
//                break;
//            }
//        }
//        if (most_played_index > -1) {
//            most_played_adapter.notifyDataSetChanged();
//        }

    };

};