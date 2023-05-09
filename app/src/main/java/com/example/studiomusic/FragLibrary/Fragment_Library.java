package com.example.studiomusic.FragLibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.AlbumView.AlbumViewActivity;
import com.example.studiomusic.BottomMenu.BottomMenu;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.Main.MainActivity;
import com.example.studiomusic.MusicData.Album;
import com.example.studiomusic.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    private static Integer page = null;
    private static Boolean more = null;
    private static Boolean loading = null;
    private static List<Album> albumsList = null;
    private static Integer position_for_adapter = null;

    private static ConstraintLayout library_loader = null;
    private static ConstraintLayout library_content = null;
    private static RecyclerView library_recyclerview = null;
    private static GridLayoutManager gridLayoutManager = null;
    private static Library_RecyclerView_Adapter adapter = null;

    private LibraryTouch libraryTouch = new LibraryTouch() {
        @Override
        public void click(int position) {
            Album album = albumsList.get(position);
            Intent intent = new Intent(getActivity(), AlbumViewActivity.class);
            intent.putExtra("album_id", album.getAlbumId());
            startActivity(intent);
        };
        @Override
        public boolean longClick(int position) {
            BottomMenu bottomMenu = new BottomMenu(BottomMenu.TYPE.HOMESCREEN_ALBUMS, getContext(), getActivity());
            bottomMenu.setAlbum(albumsList.get(position));
            bottomMenu.show();
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

        library_loader = view.findViewById(R.id.library_fragment_loader);
        library_content = view.findViewById(R.id.library_content);

        library_recyclerview = view.findViewById(R.id.library_recyclerview);
        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        library_recyclerview.setLayoutManager(gridLayoutManager);
        library_recyclerview.addOnScrollListener(onScrollListener);

        firstLoad();

        return view;

    };

    private void firstLoad() {

        page = 1;
        position_for_adapter = 0;
        more = false;
        loading = true;
        albumsList = new ArrayList<>();
        adapter = new Library_RecyclerView_Adapter(getContext(), albumsList, libraryTouch);
        library_recyclerview.setAdapter(adapter);
        library_loader.setVisibility(View.VISIBLE);
        library_content.setVisibility(View.VISIBLE);
        FetchAlbums.call(getContext());

    };

    private void nextLoads() {

        library_loader.setVisibility(View.GONE);
        library_content.setVisibility(View.VISIBLE);

        Library_RecyclerView_Adapter adapter = new Library_RecyclerView_Adapter(getContext(), albumsList, libraryTouch);
        library_recyclerview.setAdapter(adapter);

    };

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            int visibleCount = gridLayoutManager.getChildCount();
            int crossedCount = gridLayoutManager.findFirstVisibleItemPosition();
            int total = gridLayoutManager.getItemCount();
            if (
                loading != null &&
                !loading &&
                more &&
                (visibleCount + crossedCount) >= total - 10
            ) {
                page++;
                FetchAlbums.call(getContext());
                loading = true;
            }
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    private static class FetchAlbums {

        private static Context context = null;

        private FetchAlbums() {}

        private static void finish() {
            loading = false;
            new Handler().postDelayed(() -> {
                library_loader.setVisibility(View.GONE);
            }, 500);
        }

        private static void addToList(JSONArray data) {
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
        }

        private static void response(JSONObject response) {
            try {
                JSONArray data = response.getJSONArray("data");
                more = response.getBoolean("more");
                addToList(data);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private static void responseError(VolleyError err) {
            if (APIService.isAuthOrTimeError(err)) {
                APIService.errorHandler(context, err);
                return;
            }
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG).show();
        }

        private static void apiCall() {
            JSONObject params = new JSONObject();
            try {
                params.put("page", page);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            APIService.fetchLibrary(context, params, FetchAlbums::response, FetchAlbums::responseError);
        }

        private static void call(Context activityContext) {
            if (context == null) context = activityContext;
            apiCall();
        }

    }

}