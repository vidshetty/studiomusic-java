package com.example.studiomusic.FragHomescreen;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.AlbumView.AlbumViewActivity;
import com.example.studiomusic.Audio_Controller.MusicService;
import com.example.studiomusic.BottomMenu.BottomMenu;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.MusicData.Album;
import com.example.studiomusic.MusicData.Track;
import com.example.studiomusic.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Fragment_Homescreen extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Vibrator vibrator = null;

    private static List<Album> new_releases = new ArrayList<>();
    private static List<Album> recently_added = new ArrayList<>();
    private static List<Album> most_played = new ArrayList<>();
    private static List<Track> quick_picks = new ArrayList<>();

    private ConstraintLayout fullScreen = null;
    private ScrollView content = null;
    private ConstraintLayout mostplayed_container = null;
    private RecyclerView most_played_recyclerview = null;
    private RecyclerView quickpicks_recyclerview = null;
    private RecyclerView newreleases_recyclerview = null;
    private RecyclerView recentlyadded_recyclerview = null;

    private MusicService musicService = null;

    private final QuickPicksTouch quickPicksTouch = new QuickPicksTouch() {
        @Override
        public void click(int position) {};
        @Override
        public boolean longClick(int position) {
            return false;
        };
        @Override
        public void menuClick(int position) {
            vibrator.vibrate(100);
            BottomMenu bottomMenu = new BottomMenu(BottomMenu.TYPE.HOMESCREEN_QUICKPICKS, getContext(), getActivity());
            bottomMenu.setTrack(quick_picks.get(position));
            bottomMenu.show();
        };
    };

    private final NewReleasesTouch newReleasesTouch = new NewReleasesTouch() {
        @Override
        public void click(int position) {
            Album album = new_releases.get(position);
            Intent intent = new Intent(getActivity(), AlbumViewActivity.class);
            intent.putExtra("album_id", album.getAlbumId());
            startActivity(intent);
        };
        @Override
        public boolean longClick(int position) {
            BottomMenu bottomMenu = new BottomMenu(BottomMenu.TYPE.HOMESCREEN_ALBUMS, getContext(), getActivity());
            bottomMenu.setAlbum(new_releases.get(position));
            bottomMenu.show();
            return true;
        };
    };

    private final RecentlyAddedTouch recentlyAddedTouch = new RecentlyAddedTouch() {
        @Override
        public void click(int position) {
            Album album = recently_added.get(position);
            Intent intent = new Intent(getActivity(), AlbumViewActivity.class);
            intent.putExtra("album_id", album.getAlbumId());
            startActivity(intent);
        };
        @Override
        public boolean longClick(int position) {
            BottomMenu bottomMenu = new BottomMenu(BottomMenu.TYPE.HOMESCREEN_ALBUMS, getContext(), getActivity());
            bottomMenu.setAlbum(recently_added.get(position));
            bottomMenu.show();
            return true;
        };
    };

    private final MostPlayedTouch mostPlayedTouch = new MostPlayedTouch() {
        @Override
        public void click(int position) {
            Toast.makeText(getContext(), "most played click", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(100);
        };
        @Override
        public boolean longClick(int position) {
            BottomMenu bottomMenu = new BottomMenu(BottomMenu.TYPE.HOMESCREEN_ALBUMS, getContext(), getActivity());
            bottomMenu.setAlbum(most_played.get(position));
            bottomMenu.show();
            return true;
        }
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
        musicService = MusicService.getInstance(getActivity().getApplicationContext());
        musicService.bindService();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment__homescreen, container, false);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        fullScreen = view.findViewById(R.id.homescreen_fragment_loader);
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
        
        fullScreen.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);

        APIService.getHomeAlbums(getActivity().getApplicationContext(), this::response, this::responseError);

//        start();

        return view;

    };

    private void start() {
        Log.d("servicelog", "music is playing " + musicService.isPlaying());
        if (musicService.isPlaying()) return;
        if (musicService.isPrepared()) {
            musicService.play();
            return;
        }
        musicService.setUrl(
                "",
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        musicService.play();
                    }
                }
        );
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

            if (most_played.size() > 0) {
                Most_Played_Adapter most_played_adapter = new Most_Played_Adapter(getContext(), most_played, mostPlayedTouch);
                most_played_recyclerview.setAdapter(most_played_adapter);
            }
            else mostplayed_container.setVisibility(View.GONE);

            Quick_Picks_Adapter quick_picks_adapter = new Quick_Picks_Adapter(getContext(), quick_picks, quickPicksTouch);
            quickpicks_recyclerview.setAdapter(quick_picks_adapter);

            NewReleases_Adapter newReleases_adapter = new NewReleases_Adapter(getContext(), new_releases, newReleasesTouch);
            newreleases_recyclerview.setAdapter(newReleases_adapter);

            RecentlyAdded_Adapter recentlyAdded_adapter = new RecentlyAdded_Adapter(getContext(), recently_added, recentlyAddedTouch);
            recentlyadded_recyclerview.setAdapter(recentlyAdded_adapter);

            fullScreen.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);

        }
        catch(JSONException e) {
            e.printStackTrace();
            return;
        }

    }

    private void responseError(VolleyError err) {
        if (APIService.isAuthOrTimeError(err)) {
            APIService.errorHandler(getActivity().getApplicationContext(), err);
            return;
        }
        Toast.makeText(getActivity().getApplicationContext(), "Error fetching home feed!", Toast.LENGTH_SHORT).show();
    }

}