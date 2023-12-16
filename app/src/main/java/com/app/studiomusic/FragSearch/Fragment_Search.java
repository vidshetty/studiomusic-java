package com.app.studiomusic.FragSearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.SP_Controller.SPService;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.SnackBar;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Fragment_Search extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String tag = "fragment_search";

    private String mParam1;
    private String mParam2;

    private String initial_search_key = null;
    private List<Track> tracks = null;
    private List<Album> albums = null;
    private List<Track> part_tracks = null;
    private List<Album> part_albums = null;
    private List<String> searches = null;

    private OnBackPressedCallback onBackPressedCallback = null;
    private FrameLayout search_fragment = null;
    private ConstraintLayout search_loader = null;
    private ConstraintLayout search_non_loader = null;
    private ConstraintLayout search_parent = null;
    private ConstraintLayout search_searches = null;
    private ConstraintLayout search_songs_container = null;
    private ConstraintLayout search_albums_container = null;
    private ConstraintLayout no_results_found_container = null;
    private TextView no_results_found_search_text = null;
    private RelativeLayout songs_more_button = null;
    private RelativeLayout albums_more_button = null;
    private RecyclerView search_songs_recyclerview = null;
    private LinearLayoutManager songs_layoutmanager = null;
    private Songs_Adapter songs_adapter = null;
    private RecyclerView search_albums_recyclerview = null;
    private LinearLayoutManager albums_layoutmanager = null;
    private Albums_Adapter albums_adapter = null;
    private RecyclerView search_searches_recyclerview = null;
    private SearchHistoryAdapter search_history_adapter = null;

    private ImageView search_clear = null;
    private EditText search_edittext = null;

    public SongsTouch songsTouch = new SongsTouch() {
        @Override
        public void click(int position) {
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .setNowPlayingTrack(part_tracks.get(position));
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

    public AlbumsTouch albumsTouch = new AlbumsTouch() {
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

    public Fragment_Search() {
        // Required empty public constructor
    };

    public static Fragment_Search newInstance(String param1, String param2) {
        Fragment_Search fragment = new Fragment_Search();
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
            initial_search_key = getArguments().getString("query", null);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment__search, container, false);

        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.black));

        search_fragment = view.findViewById(R.id.search_fragment);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) search_fragment.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getContext());
        search_fragment.setLayoutParams(params);

        search_loader = view.findViewById(R.id.search_loader);
        search_parent = view.findViewById(R.id.search_parent);
        search_searches = view.findViewById(R.id.search_searches);

        search_non_loader = view.findViewById(R.id.search_non_loader);
        search_non_loader.setVisibility(View.VISIBLE);

        search_songs_container = view.findViewById(R.id.search_songs_container);
        search_albums_container = view.findViewById(R.id.search_albums_container);
        no_results_found_container = view.findViewById(R.id.no_results_found_container);
        no_results_found_search_text = view.findViewById(R.id.no_results_found_search_text);

        search_songs_recyclerview = view.findViewById(R.id.search_songs_recyclerview);
        songs_layoutmanager = new LinearLayoutManager(getContext());
        search_songs_recyclerview.setLayoutManager(songs_layoutmanager);

        search_albums_recyclerview = view.findViewById(R.id.search_albums_recyclerview);
        albums_layoutmanager = new LinearLayoutManager(getContext());
        search_albums_recyclerview.setLayoutManager(albums_layoutmanager);

        search_clear = view.findViewById(R.id.search_clear);
        search_edittext = view.findViewById(R.id.search_edittext);

        songs_more_button = view.findViewById(R.id.songs_more_button);
        songs_more_button.setOnClickListener(v -> {
            handleMoreButtonClick(MusicApplication.MORE_SEARCH_TYPES.TRACKS);
        });
        albums_more_button = view.findViewById(R.id.albums_more_button);
        albums_more_button.setOnClickListener(v -> {
            handleMoreButtonClick(MusicApplication.MORE_SEARCH_TYPES.ALBUMS);
        });

        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchEnter();
                    return true;
                }
                return false;
            }
        });

        search_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {};
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() < 1) search_clear.setVisibility(View.GONE);
                else search_clear.setVisibility(View.VISIBLE);
            };
            @Override
            public void afterTextChanged(Editable editable) {};
        });

        search_clear.setOnClickListener(v -> {
            search_edittext.setText("");
//            setSearchesData();
//            search_searches.setVisibility(View.VISIBLE);
//            search_parent.setVisibility(View.GONE);
            albums = null;
            tracks = null;
            checkView();
            if (onBackPressedCallback != null) onBackPressedCallback.setEnabled(false);
        });

        search_searches_recyclerview = view.findViewById(R.id.search_searches_recyclerview);

        if (onBackPressedCallback == null) {
            onBackPressedCallback = new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    search_clear.performClick();
                    this.setEnabled(false);
                };
            };
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
        }

        if (initial_search_key != null) {
            search_edittext.setText(initial_search_key);
            searchEnter();
            return view;
        }

        checkView();

        return view;

    };

    private void searchEnter() {
        search_edittext.clearFocus();
        hideKeyboard(search_edittext.getWindowToken());
        callApi();
        addToSearchHistory();
    };

    private void handleMoreButtonClick(MusicApplication.MORE_SEARCH_TYPES type) {

        Common.vibrate(getContext(), 50);

        JSONArray array = new JSONArray();

        if (type == MusicApplication.MORE_SEARCH_TYPES.TRACKS) {
            for (int i=0; i<tracks.size(); i++) {
                array.put(tracks.get(i).getJSONObject());
            }
        }

        if (type == MusicApplication.MORE_SEARCH_TYPES.ALBUMS) {
            for (int i=0; i<albums.size(); i++) {
                array.put(albums.get(i).getJSONObject());
            }
        }

        ((MainActivity) getActivity()).openMoreSearchFragment(array.toString(), type);

    };

    public void setQuery(String query) {
        Log.d("search_intent", "query sent " + query);
        initial_search_key = query;
        search_edittext.setText(query);
        searchEnter();
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            search_non_loader.setVisibility(View.VISIBLE);
            if (onBackPressedCallback != null) onBackPressedCallback.setEnabled(false);
        }
        else {
            search_non_loader.setVisibility(View.VISIBLE);
            if (
                onBackPressedCallback != null &&
                search_parent != null &&
                search_parent.getVisibility() == View.VISIBLE
            ) onBackPressedCallback.setEnabled(true);
            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(search_non_loader);
                TransitionManager.beginDelayedTransition(search_fragment, transition);
                search_non_loader.setVisibility(View.GONE);
            }, 0);
        }
    };

    private void checkView() {

        search_non_loader.setVisibility(View.VISIBLE);

        boolean noTextExists = search_edittext.getText().toString().equals("");
        boolean noDataExists = albums == null && tracks == null;

        if (noTextExists && noDataExists) {
            setSearchesData();
            search_searches.setVisibility(View.VISIBLE);
            search_parent.setVisibility(View.GONE);
        }
        else {
            setParentData();
            search_searches.setVisibility(View.GONE);
            search_parent.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(() -> {
            Transition transition = new Fade();
            transition.setDuration(300);
            transition.addTarget(search_non_loader);
            TransitionManager.beginDelayedTransition(search_fragment, transition);
            search_non_loader.setVisibility(View.GONE);
        }, 0);

    };

    private void setSearchesData() {

        searches = getSearchHistoryList();

        if (searches == null || searches.isEmpty()) {
            search_searches_recyclerview.setVisibility(View.GONE);
            return;
        }
        else search_searches_recyclerview.setVisibility(View.VISIBLE);

        SearchHistoryTouch searchHistoryTouch = new SearchHistoryTouch() {
            @Override
            public void click(int position) {
                String text = searches.get(position);
                search_edittext.setText(text);
                search_edittext.clearFocus();
                hideKeyboard(search_edittext.getWindowToken());
                callApi();
                addToSearchHistory();
            }

            @Override
            public boolean longClick(int position) {

                View.OnClickListener remove = v -> {
                    removeFromSearchHistory(position);
                };

                List<BottomMenu.MenuItem> list = new ArrayList<>();
                list.add(new BottomMenu.MenuItem(null, "Remove From Search History", remove));

                BottomMenu bottomMenu = new BottomMenu.Builder()
                        .setContext(getContext())
                        .setDataList(list)
                        .build();

                bottomMenu.show();

                return true;

            };
        };

        search_history_adapter = new SearchHistoryAdapter(getContext(), searches, searchHistoryTouch);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        search_searches_recyclerview.setLayoutManager(linearLayoutManager);
        search_searches_recyclerview.setAdapter(search_history_adapter);

    };

    private void removeFromSearchHistory(int position) {

        if (search_history_adapter != null) {
            search_history_adapter.notifyItemRemoved(position);
        }

        if (searches != null) {
            searches.remove(position);
        }

        JSONArray jsonArray = new JSONArray();
        for (int i=0; i<searches.size(); i++) {
            try {
                jsonArray.put(i, searches.get(i));
            } catch (JSONException e) {
                continue;
            }
        }

        SharedPreferences history = SPService.SEARCH_HISTORY(getContext());
        SharedPreferences.Editor editor = history.edit();
        editor.putString("search_history", jsonArray.toString());
        editor.apply();

    };

    private List<String> getSearchHistoryList() {

        try {

            String search_string = SPService.SEARCH_HISTORY(getContext()).getString("search_history", "[]");
            List<String> searches = new ArrayList<>();

            JSONArray jsonArray = null;

            jsonArray = new JSONArray(search_string);
            for (int i=0; i<jsonArray.length(); i++) {
                searches.add(jsonArray.getString(i));
            }

            return searches;

        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    };

    private void setParentData() {

        onBackPressedCallback.setEnabled(true);

        boolean isAlbumsEmpty = part_albums == null || part_albums.isEmpty();
        boolean isTracksEmpty = part_tracks == null || part_tracks.isEmpty();

        if (isAlbumsEmpty && isTracksEmpty) {
            search_songs_container.setVisibility(View.GONE);
            search_albums_container.setVisibility(View.GONE);
            no_results_found_container.setVisibility(View.VISIBLE);
            no_results_found_search_text.setText(
                new StringBuilder()
                    .append("'")
                    .append(search_edittext.getText().toString()).append("'")
                    .toString()
            );
            return;
        }
        else {
            no_results_found_container.setVisibility(View.GONE);
        }

        if (isTracksEmpty) {
            search_songs_container.setVisibility(View.GONE);
        }
        else {
            search_songs_container.setVisibility(View.VISIBLE);
            songs_adapter = new Songs_Adapter(getContext(), part_tracks, songsTouch);
            search_songs_recyclerview.setAdapter(songs_adapter);
        }

        if (isAlbumsEmpty) {
            search_albums_container.setVisibility(View.GONE);
        }
        else {
            search_albums_container.setVisibility(View.VISIBLE);
            albums_adapter = new Albums_Adapter(getContext(), part_albums, albumsTouch);
            search_albums_recyclerview.setAdapter(albums_adapter);
        }

        setMoreButtonVisibility();

    };

    private void setMoreButtonVisibility() {

        if (part_tracks.size() < tracks.size()) songs_more_button.setVisibility(View.VISIBLE);
        else songs_more_button.setVisibility(View.GONE);

        if (part_albums.size() < albums.size()) albums_more_button.setVisibility(View.VISIBLE);
        else albums_more_button.setVisibility(View.GONE);

    };

    private void response(JSONObject response) {

        try {

            JSONArray tracks_array = response.getJSONArray("tracks");
            tracks = new ArrayList<>();
            part_tracks = new ArrayList<>();
            for (int i=0; i<tracks_array.length(); i++) {
                Track each = Common.buildTrack(tracks_array.getJSONObject(i));
                tracks.add(each);
                if (i < 5) part_tracks.add(each);
            }

//            songs_adapter = new Songs_Adapter(getContext(), part_tracks, songsTouch);
//            search_songs_recyclerview.setAdapter(songs_adapter);

            JSONArray albums_array = response.getJSONArray("albums");
            albums = new ArrayList<>();
            part_albums = new ArrayList<>();
            for (int i=0; i<albums_array.length(); i++) {
                Album each = Common.buildAlbum(albums_array.getJSONObject(i));
                albums.add(each);
                if (i < 5) part_albums.add(each);
            }

//            albums_adapter = new Albums_Adapter(getContext(), part_albums, albumsTouch);
//            search_albums_recyclerview.setAdapter(albums_adapter);
//
//            setMoreButtonVisibility();
//
//            search_searches.setVisibility(View.GONE);
//            search_parent.setVisibility(View.VISIBLE);

            checkView();

            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(search_loader);
                TransitionManager.beginDelayedTransition(search_fragment, transition);
                search_loader.setVisibility(View.GONE);
            }, 0);

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    };

    private void responseError(VolleyError err) {
        new Handler().postDelayed(() -> {
            Transition transition = new Fade();
            transition.setDuration(300);
            transition.addTarget(search_loader);
            TransitionManager.beginDelayedTransition(search_fragment, transition);
            search_loader.setVisibility(View.GONE);
        }, 0);
    };

    private void callApi() {
        try {
            JSONObject query = new JSONObject();
            query.put("name", search_edittext.getText().toString());
            APIService.search(getContext(), query, this::response, this::responseError);
            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(search_loader);
                TransitionManager.beginDelayedTransition(search_fragment, transition);
                search_loader.setVisibility(View.VISIBLE);
            }, 0);

        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    };

    private void addToSearchHistory() {

        String text = search_edittext.getText().toString().trim();

        SharedPreferences history_sp = SPService.SEARCH_HISTORY(getContext());
        String search_string = history_sp.getString("search_history", "[]");
        List<String> searches = new ArrayList<>();

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(search_string);
            for (int i=0; i<jsonArray.length(); i++) {
                if (text.equals(jsonArray.getString(i))) continue;
                searches.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        searches.add(0, text);

        jsonArray = new JSONArray(searches.subList(0, Math.min(8, searches.size())));

        Log.d("search_log", jsonArray.toString());

        SharedPreferences.Editor editor = history_sp.edit();
        editor.putString("search_history", jsonArray.toString());
        editor.apply();

    };

    private void hideKeyboard(IBinder token) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    };

    private void openTrackMenu(int position) {

        Track track = tracks.get(position);

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

    private void openAlbumMenu(int position) {

        Album album = albums.get(position);

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

    private void openAlbumViewFragment(String albumId) {

        ((MainActivity) getActivity()).openAlbumViewFragment(albumId);

//        Fragment_AlbumView fragment = new Fragment_AlbumView();
//        Bundle bundle = new Bundle();
//        bundle.putString("album_id", albumId);
//        fragment.setArguments(bundle);
//
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.main_frame_layout, fragment, "ALBUMVIEW_FRAGMENT_TAG")
//                .addToBackStack(null)
//                .commit();

    };

};