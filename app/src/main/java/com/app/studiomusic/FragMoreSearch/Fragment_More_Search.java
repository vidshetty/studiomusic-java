package com.app.studiomusic.FragMoreSearch;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.SnackBar;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Fragment_More_Search extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private TextView toolbar_textview = null;
    private ImageView toolbar_backbutton = null;
    private ImageView toolbar_menubutton = null;
    private RecyclerView recyclerView = null;
    private ConstraintLayout more_search_non_loader = null;
    private ConstraintLayout more_search_parent = null;
    private ConstraintLayout more_search_status_bar = null;

    private List<Album> albums = null;
    private List<Track> tracks = null;
    private MusicApplication.MORE_SEARCH_TYPES type = null;
    private MoreSearchSongsAdapter songs_adapter = null;
    private MoreSearchAlbumsAdapter albums_adapter = null;

    private SongsTouch songsTouch = new SongsTouch() {
        @Override
        public void click(int position) {
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .setNowPlayingTrack(tracks.get(position));
            MusicService.start(getContext());
        };
        @Override
        public boolean longClick(int position) {
            openTrackMenu(position);
            return true;
        };
        @Override
        public void menuClick(int position) {
            Common.vibrate(getContext(), 50);
            openTrackMenu(position);
        };
    };

    private AlbumsTouch albumsTouch = new AlbumsTouch() {
        @Override
        public void click(int position) {
            Album album = albums.get(position);
            openAlbumViewFragment(album.getAlbumId());
        };
        @Override
        public boolean longClick(int position) {
            openAlbumMenu(position);
            return true;
        };
        @Override
        public void menuClick(int position) {
            Common.vibrate(getContext(), 50);
            openAlbumMenu(position);
        };
    };

    public Fragment_More_Search() {};

    public static Fragment_More_Search newInstance(String param1, String param2) {
        Fragment_More_Search fragment = new Fragment_More_Search();
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
            setInitialData(getArguments());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment__more__search, container, false);

        int status_height = Common.getStatusBarHeight(getContext());

        if (more_search_status_bar == null) more_search_status_bar = view.findViewById(R.id.more_search_status_bar);
        ViewGroup.LayoutParams params = more_search_status_bar.getLayoutParams();
        params.height = status_height;
        more_search_status_bar.setLayoutParams(params);

        if (more_search_parent == null) more_search_parent = view.findViewById(R.id.more_search_parent);

        if (more_search_non_loader == null) more_search_non_loader = view.findViewById(R.id.more_search_non_loader);
        more_search_non_loader.setVisibility(View.VISIBLE);

        if (toolbar_textview == null) toolbar_textview = view.findViewById(R.id.more_search_toolbar_text);
        if (type == MusicApplication.MORE_SEARCH_TYPES.TRACKS) {
            toolbar_textview.setText("Songs");
        }
        if (type == MusicApplication.MORE_SEARCH_TYPES.ALBUMS) {
            toolbar_textview.setText("Albums");
        }

        if (toolbar_backbutton == null) toolbar_backbutton = view.findViewById(R.id.more_search_toolbar_backbutton);
        toolbar_backbutton.setOnClickListener(v -> {
            onBackPress(true);
        });

        if (toolbar_menubutton == null) toolbar_menubutton = view.findViewById(R.id.more_search_toolbar_menu);
        toolbar_menubutton.setOnClickListener(v -> {
            if (type == MusicApplication.MORE_SEARCH_TYPES.TRACKS) openTrackRootMenu();
            if (type == MusicApplication.MORE_SEARCH_TYPES.ALBUMS) openAlbumRootMenu();
        });

        if (recyclerView == null) recyclerView = view.findViewById(R.id.more_search_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        if (type == MusicApplication.MORE_SEARCH_TYPES.TRACKS) {
            songs_adapter = new MoreSearchSongsAdapter(getContext(), tracks, songsTouch);
            recyclerView.setAdapter(songs_adapter);
        }

        if (type == MusicApplication.MORE_SEARCH_TYPES.ALBUMS) {
            albums_adapter = new MoreSearchAlbumsAdapter(getContext(), albums, albumsTouch);
            recyclerView.setAdapter(albums_adapter);
        }

        new Handler().postDelayed(() -> {
            Transition transition = new Fade();
            transition.setDuration(300);
            transition.addTarget(more_search_non_loader);
            TransitionManager.beginDelayedTransition(more_search_parent, transition);
            more_search_non_loader.setVisibility(View.GONE);
        }, 0);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPress(false);
            };
        });

        return view;

    };

    private void setInitialData(Bundle bundle) {

        String data_type = bundle.getString("type");
        if (data_type.equals(MusicApplication.MORE_SEARCH_TYPES.TRACKS.toString())) {
            type = MusicApplication.MORE_SEARCH_TYPES.TRACKS;
        } else if (data_type.equals(MusicApplication.MORE_SEARCH_TYPES.ALBUMS.toString())) {
            type = MusicApplication.MORE_SEARCH_TYPES.ALBUMS;
        }

        String json_string = bundle.getString("data");

        try {

            JSONArray array = new JSONArray(json_string);

            if (type == MusicApplication.MORE_SEARCH_TYPES.TRACKS) {
                tracks = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    Track t = Common.buildTrack(array.getJSONObject(i));
                    tracks.add(t);
                }
            }

            if (type == MusicApplication.MORE_SEARCH_TYPES.ALBUMS) {
                albums = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    Album a = Common.buildAlbum(array.getJSONObject(i));
                    albums.add(a);
                }
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    };

    private void closeFragment() {
        ((MainActivity) getActivity()).backPressFromMoreSearchView();
    };

    private void onBackPress(boolean vibrate) {
        if (vibrate) Common.vibrate(getContext(), 50);
        closeFragment();
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            more_search_non_loader.setVisibility(View.VISIBLE);
        }
        else {
            more_search_non_loader.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(more_search_non_loader);
                TransitionManager.beginDelayedTransition(more_search_parent, transition);
                more_search_non_loader.setVisibility(View.GONE);
            }, 0);
        }
    };

    private void openTrackMenu(int position) {

        Track track = tracks.get(position);

        View.OnClickListener goToHome = view -> {
            openAlbumViewFragment(track.getAlbumId());
        };

        View.OnClickListener share = view -> {
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

    private void openTrackRootMenu() {

        Common.vibrate(getContext(), 50);

        View.OnClickListener playAll = view -> {

            MusicForegroundService.NowPlayingData.getInstance(getContext())
                    .setNowPlayingTrack(tracks.get(0));

            MusicService.start(getContext());

            if (tracks.size() < 2) return;

            for (int i=1; i<tracks.size(); i++) {
                MusicForegroundService.NowPlayingData.getInstance(getContext())
                        .addToQueue(tracks.get(i));
            }

        };

        View.OnClickListener playNext = view -> {
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Playing all tracks next", 2000);
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .playNextMultipleTracks(tracks);
//            Toast.makeText(getContext(), "Playing all tracks next", Toast.LENGTH_SHORT).show();
        };

        View.OnClickListener addToQueue = view -> {
            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Added all tracks to queue", 2000);
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .addToQueueMultipleTracks(tracks);
//            Toast.makeText(getContext(), "Added all tracks to queue", Toast.LENGTH_SHORT).show();
        };

        List<BottomMenu.MenuItem> menu_list = Arrays.asList(
                new BottomMenu.MenuItem(null, "Play All", playAll),
                new BottomMenu.MenuItem(null, "Play All Tracks Next", playNext),
                new BottomMenu.MenuItem(null, "Add All Tracks To Queue", addToQueue)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(getContext())
                .setDataList(menu_list)
                .build();

        bottomMenu_new.show();

    };

    private void openAlbumMenu(int position) {

        Album album = albums.get(position);

        View.OnClickListener goToHome = view -> {
            openAlbumViewFragment(album.getAlbumId());
        };

        View.OnClickListener share = view -> {
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

    private void openAlbumRootMenu() {

        Common.vibrate(getContext(), 50);

        View.OnClickListener playNext = view -> {

            List<Track> track_list = new ArrayList<>();

            for (int i=0; i<albums.size(); i++) {
                for (int j=0; i<albums.get(i).getTracks().size(); i++) {
                    track_list.add(albums.get(i).getTracks().get(j));
                }
            }

            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .playNextMultipleTracks(track_list);

            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Playing all albums next", 2000);

//            Toast.makeText(getContext(), "Playing all albums next", Toast.LENGTH_SHORT).show();

        };

        View.OnClickListener addToQueue = view -> {

            List<Track> track_list = new ArrayList<>();

            for (int i=0; i<albums.size(); i++) {
                for (int j=0; i<albums.get(i).getTracks().size(); i++) {
                    track_list.add(albums.get(i).getTracks().get(j));
                }
            }

            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .addToQueueMultipleTracks(track_list);

            SnackBar.make(getContext(), SnackBar.getMainActivityView(), "Added all albums to queue", 2000);

//            Toast.makeText(getContext(), "Added all albums to queue", Toast.LENGTH_SHORT).show();

        };

        List<BottomMenu.MenuItem> menu_list = Arrays.asList(
                new BottomMenu.MenuItem(null, "Play All Albums Next", playNext),
                new BottomMenu.MenuItem(null, "Add All Albums To Queue", addToQueue)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(getContext())
                .setDataList(menu_list)
                .build();

        bottomMenu_new.show();

    };

    private void openAlbumViewFragment(String albumId) {
        ((MainActivity) getActivity()).openAlbumViewFragment(albumId);
    };

};