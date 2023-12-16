package com.app.studiomusic.FragLibrary;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.CustomSwipeRefreshLayout.CustomSwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.ProfileImage;
import com.app.studiomusic.Common.SnackBar;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.MusicData.Album;
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

public class Fragment_Library extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Fragment_Library() {}

    public static Fragment_Library newInstance(String param1, String param2) {
        Fragment_Library fragment = new Fragment_Library();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    };

    private Integer page = null;
    private Boolean more = null;
    private Boolean loading = null;
    private List<Album> albumsList = null;
    private Integer position_for_adapter = null;

    private CustomSwipeRefreshLayout swipeRefreshLayout = null;
    private CoordinatorLayout parent = null;
    private ImageView profileImage = null;
    private ConstraintLayout library_loader = null;
    private ConstraintLayout library_nonloader = null;
    private RecyclerView library_recyclerview = null;
    private GridLayoutManager gridLayoutManager = null;
    private Library_RecyclerView_Adapter adapter = null;

    private final LibraryTouch libraryTouch = new LibraryTouch() {
        @Override
        public void click(int position) {
            openAlbumViewFragment(albumsList.get(position).getAlbumId());
        };
        @Override
        public boolean longClick(int position) {
            openAlbumMenu(position);
            return true;
        };
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

        View view = inflater.inflate(R.layout.fragment__library, container, false);

        getActivity().getWindow().setStatusBarColor(getActivity().getColor(R.color.black));

        parent = view.findViewById(R.id.library_parent);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) parent.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getContext());
        parent.setLayoutParams(params);

        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);

        swipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firstLoadData();
                api_call();
            }
        });

        profileImage = view.findViewById(R.id.profileImage);

        library_loader = view.findViewById(R.id.library_fragment_loader);

        library_nonloader = view.findViewById(R.id.library_fragment_nonloader);

        library_recyclerview = view.findViewById(R.id.library_recyclerview);
        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        library_recyclerview.setLayoutManager(gridLayoutManager);
        library_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleCount = gridLayoutManager.getChildCount();
                int crossedCount = gridLayoutManager.findFirstVisibleItemPosition();
                int total = gridLayoutManager.getItemCount();
                if (
                        loading != null && !loading &&
                        more && (visibleCount + crossedCount) >= total - 10
                ) {
                    page++;
                    loading = true;
                    api_call();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        if (albumsList != null) {

            library_loader.setVisibility(View.GONE);
            library_nonloader.setVisibility(View.VISIBLE);

            library_recyclerview.setAdapter(adapter);

            new Handler().postDelayed(() -> {
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(library_nonloader);
                TransitionManager.beginDelayedTransition(parent, transition);
                library_nonloader.setVisibility(View.GONE);
            }, 0);

//            Transition transition = new Fade();
//            transition.setDuration(600);
//            transition.addTarget(library_loader);
//            TransitionManager.beginDelayedTransition(parent, transition);
//            library_loader.setVisibility(View.GONE);

        }
        else {

            library_loader.setVisibility(View.VISIBLE);
            library_nonloader.setVisibility(View.GONE);

            firstLoadData();
            api_call();

        }

        setProfileImage();

        return view;

    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("library_hidden", hidden + "");
        if (hidden) {
            library_nonloader.setVisibility(View.VISIBLE);
        }
        else {

            library_nonloader.setVisibility(View.VISIBLE);
            library_loader.setVisibility(View.GONE);

            new Handler().postDelayed(() -> {
                Log.d("library_hidden", "running");
                Transition transition = new Fade();
                transition.setDuration(300);
                transition.addTarget(library_nonloader);
                TransitionManager.beginDelayedTransition(parent, transition);
                library_nonloader.setVisibility(View.GONE);
            }, 0);

        }
    };

    private void firstLoadData() {
        page = 1;
//        albumsList = new ArrayList<>();
        position_for_adapter = 0;
        more = false;
        loading = true;
//        adapter = new Library_RecyclerView_Adapter(getContext(), albumsList, libraryTouch);
//        library_recyclerview.setAdapter(adapter);
    };

    private void setProfileImage() {

        profileImage.setOnClickListener(view -> {
            Common.vibrate(getContext(), 50);
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account == null) return;

        Glide.with(this)
                .load(account.getPhotoUrl() == null ? ProfileImage.getAlternativePicture(account.getDisplayName()) : account.getPhotoUrl())
                .into(profileImage);

    };

    private void api_call() {
        JSONObject params = new JSONObject();
        try {
            params.put("page", page);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        APIService.fetchLibrary(getContext(), params, this::response, this::responseError);
    };

    private void response(JSONObject response) {
        if (page == 1) {
            albumsList = new ArrayList<>();
            adapter = new Library_RecyclerView_Adapter(getContext(), albumsList, libraryTouch);
            library_recyclerview.setAdapter(adapter);
        }
        try {
            JSONArray data = response.getJSONArray("data");
            Log.d("response_length", "page " + page + " size " + data.length());
            more = response.getBoolean("more");
            addToList(data);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private void addToList(JSONArray data) {
        for (int i=0; i<data.length(); i++) {
            try {

                JSONObject each = data.getJSONObject(i);
                Album album = Common.buildAlbum(each);

                albumsList.add(album);
                adapter.notifyItemInserted(position_for_adapter);
                position_for_adapter++;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void finish() {

        loading = false;

        new Handler().postDelayed(() -> {
            Transition transition = new Fade();
            transition.setDuration(300);
            transition.addTarget(library_loader);
            TransitionManager.beginDelayedTransition(parent, transition);
            library_loader.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }, 0);

    };

    private void responseError(VolleyError err) {
        Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
    };

    private void openAlbumViewFragment(String albumId) {

        ((MainActivity) getActivity()).openAlbumViewFragment(albumId);

//        Fragment_AlbumView fragment = new Fragment_AlbumView();
//        Bundle bundle = new Bundle();
//        bundle.putString("album_id", albumId);
//        fragment.setArguments(bundle);
//
//        Log.d("fragment activity", getActivity() == null ? "true" : "False");
//        Log.d("fragment manager", getActivity().getSupportFragmentManager() == null ? "true" : "false");
//
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.main_frame_layout, fragment, "ALBUMVIEW_FRAGMENT_TAG")
//                .addToBackStack(null)
//                .commit();

    };

    private void openAlbumMenu(int position) {

        Album album = albumsList.get(position);

        View.OnClickListener goToAlbum = view -> {
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
                new BottomMenu.MenuItem(null, "Go To Album", goToAlbum),
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

};
