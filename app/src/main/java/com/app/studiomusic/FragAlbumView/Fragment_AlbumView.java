package com.app.studiomusic.FragAlbumView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Audio_Controller.MusicService;
import com.bumptech.glide.Glide;
import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.SnackBar;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Fragment_AlbumView extends Fragment {

    private static final String tag = "albumview";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context context = null;
    private FragmentActivity activity = null;
    private PlayPauseReceiver playPauseReceiver = null;

    private static String albumId = null;
    private static boolean should_play = false;
    private static boolean fragment_destroyed = false;
    private static String trackId = null;
    private static FrameLayout parent = null;
    private static ImageView backButton = null;
    private static ImageView loader_backButton = null;
    private static TextView album_name = null;
    private static ImageView album_art = null;
    private static Album album = null;
    private static ConstraintLayout main = null;
    private static ConstraintLayout albumview_content = null;
    private static ConstraintLayout albumview_fragment_loader = null;
    private static RelativeLayout albumview_header = null;
    private static TextView album_artist = null;
    private static TextView album_type = null;
    private static TextView album_year = null;
    private static Toolbar toolbar = null;
    private static Toolbar loader_toolbar = null;
    private static NestedScrollView scrollView = null;
    private static ConstraintLayout loader = null;
    private static RecyclerView track_listview = null;
    private static Album_Track_Adapter adapter = null;
    private static TextView total_tracks = null;
    private static TextView release_date_textview = null;
    private static TextView total_duration = null;
    private static ImageView albumview_menu = null;
    private static ImageView album_playbutton = null;
//    private static ImageView album_pausebutton = null;

    private final AlbumTracksTouch albumTracksTouch = new AlbumTracksTouch() {
        @Override
        public void trackClick(int position) {

            MusicForegroundService.NowPlayingData.getInstance(context)
                .setNowPlayingTrack(album.getTracks().get(position));
            MusicService.start(context);
            adapter.notifyDataSetChanged();

            int limit = album.getTracks().size() - 1;
            while (limit > 0) {
                position = (position+1) % album.getTracks().size();
                MusicForegroundService.NowPlayingData.getInstance(context)
                    .addToQueue(album.getTracks().get(position));
                limit--;
            }

        };
        @Override
        public void albumMenuClick() {
            Common.vibrate(getContext(), 50);
            openAlbumMenu();
        };
        @Override
        public void trackMenuClick(int position) {
            openTrackMenu(position);
            Common.vibrate(getContext(), 50);
        };
    };

    private final class PlayPauseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTrack();
        };
    };

    public Fragment_AlbumView() {
        // Required empty public constructor
    };

    public static Fragment_AlbumView newInstance(String param1, String param2) {
        Fragment_AlbumView fragment = new Fragment_AlbumView();
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
            albumId = getArguments().getString("album_id");
            should_play = getArguments().getBoolean("should_play", false);
            trackId = getArguments().getString("track_id", null);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(playPauseReceiver);
        playPauseReceiver = null;
        fragment_destroyed = true;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment__album_view, container, false);

        fragment_destroyed = false;

        album = null;
        context = getContext();
        activity = getActivity();

        IntentFilter intentFilter = new IntentFilter(MusicApplication.PLAY_PAUSE);
        if (playPauseReceiver == null) playPauseReceiver = new PlayPauseReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(playPauseReceiver, intentFilter);

        activity.getWindow().setStatusBarColor(activity.getColor(R.color.black));

        main = view.findViewById(R.id.albumview_mainlayout);

        loader_toolbar = view.findViewById(R.id.albumview_loader_toolbar);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) loader_toolbar.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getContext());
        loader_toolbar.setLayoutParams(params);

        albumview_content = view.findViewById(R.id.albumview_content);

        albumview_fragment_loader = view.findViewById(R.id.albumview_fragment_loader);

        albumview_header = view.findViewById(R.id.albumview_header);

        parent = view.findViewById(R.id.albumview_parent);

        backButton = view.findViewById(R.id.albumview_backButton);
        backButton.setOnClickListener(v -> onBackPress(true));

//        loader_backButton = view.findViewById(R.id.albumview_backButton_loader);
//        loader_backButton.setOnClickListener(v -> onBackPress(true));

        album_playbutton = view.findViewById(R.id.album_playbutton);
//        album_pausebutton = view.findViewById(R.id.album_pausebutton);
        albumview_menu = view.findViewById(R.id.albumview_menu);
        album_name = view.findViewById(R.id.albumview_name);
        album_art = view.findViewById(R.id.albumview_albumart);
        album_artist = view.findViewById(R.id.albumview_albumartist);
        album_type = view.findViewById(R.id.albumview_type);
        album_year = view.findViewById(R.id.albumview_year);
        toolbar = view.findViewById(R.id.albumviewToolbar);
        scrollView = view.findViewById(R.id.albumview_scrollview);
        loader = view.findViewById(R.id.albumview_loader);
        total_tracks = view.findViewById(R.id.totaltracks);
        release_date_textview = view.findViewById(R.id.release_date);
        total_duration = view.findViewById(R.id.total_duration);
        track_listview = view.findViewById(R.id.albumview_tracklist);
        track_listview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        album_playbutton.setOnClickListener(v -> playAndAddAlbumToQueue(true));

//        album_pausebutton.setOnClickListener(v -> pauseAlbum());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPress(false);
            };
        });

        JSONObject query = new JSONObject();
        try {
            query.put("albumId", albumId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIService.getAlbum(context, query, this::response, this::responseError);

        new Handler().postDelayed(() -> {
            albumview_fragment_loader.setVisibility(View.VISIBLE);
        }, 0);

        return view;

    };

    private void closeFragment() {

//        FragmentManager fragmentManager = activity.getSupportFragmentManager();
//        if (fragmentManager.getBackStackEntryCount() == 0) {
//            return;
//        }
//        for (int i=0; i<fragmentManager.getBackStackEntryCount(); i++) {
//            fragmentManager.popBackStack();
//        }

        ((MainActivity) getActivity()).backPressFromAlbumView();

    };

    private void response(JSONObject response) {

        if (response == null) {
            Log.d("servicelog", "response is null");
            return;
        }

        if (fragment_destroyed) return;

        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        params1.topMargin = Common.getStatusBarHeight(getContext());
        toolbar.setLayoutParams(params1);

        ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) albumview_header.getLayoutParams();
        params2.topMargin = Common.getStatusBarHeight(getContext());
        albumview_header.setLayoutParams(params2);

        album = Common.buildAlbum(response);
        if (album == null) return;

        Glide.with(context)
                .asBitmap()
                .load(album.getThumbnail())
                .into(album_art);

        album_name.setText(album.getAlbum());
        album_artist.setText(album.getAlbumArtist());
        album_type.setText(album.getType());
        album_year.setText(album.getYear());

        this.setBackground();

        List<Track> tracks = album.getTracks();
        adapter = new Album_Track_Adapter(context, tracks, albumTracksTouch);
        track_listview.setAdapter(adapter);

        albumview_menu.setOnClickListener(view -> {
            albumTracksTouch.albumMenuClick();
        });

        total_tracks.setText(new StringBuilder().append(tracks.size()).append(tracks.size() > 1 ? " songs" : " song").toString());
        total_duration.setText(getTotalDuration(tracks));

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.sss'Z'").parse(album.getReleaseDate());
            String formatted_date = new SimpleDateFormat("MMMM dd yyyy").format(date);
            release_date_textview.setText(formatted_date);
        } catch (ParseException e) {
            release_date_textview.setText("");
        }

//        if (isNowPlayingSameAlbum() && NowPlayingData.getInstance(getContext()).getMediaIsPlaying()) {
//            album_pausebutton.setVisibility(View.VISIBLE);
//            album_playbutton.setVisibility(View.GONE);
//        }
//        else {
//            album_pausebutton.setVisibility(View.GONE);
//            album_playbutton.setVisibility(View.VISIBLE);
//        }

//        Transition transition = new Fade();
//        transition.setDuration(100);
//        transition.addTarget(R.id.albumview_loader);
//        TransitionManager.beginDelayedTransition(findViewById(R.id.albumview_parent), transition);

//        new Handler().postDelayed(() -> {
//            Transition transition = new Fade();
//            transition.setDuration(300);
//            transition.addTarget(loader);
//            TransitionManager.beginDelayedTransition(parent, transition);
//            loader.setVisibility(View.GONE);
//        }, 0);

        loader.setVisibility(View.GONE);

        if (should_play) {
            if (trackId != null) {
                int index = -1;
                for (int i=0; i<tracks.size(); i++) {
                    if (tracks.get(i).getTrackId().equals(trackId)) {
                        index = i;
                        break;
                    }
                }
                if (index > -1) {
                    albumTracksTouch.trackClick(index);
                }
            }
            else {
                playAndAddAlbumToQueue(false);
            }
        }

    };

    private void responseError(VolleyError err) {
        Toast.makeText(context, "Error finding the album!", Toast.LENGTH_SHORT).show();
    };

    private static String convert(String color) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        int nred = (int) ((int) red + (-red * 0.5));
        int ngreen = (int) ((int) green + (-green * 0.5));
        int nblue = (int) ((int) blue + (-blue * 0.5));
        String hex = String.format("#%02x%02x%02x", nred, ngreen, nblue);
        return hex;
//        return "#80" + hex.substring(1);
    };

    private static int convert(String color, double percent) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        return Color.rgb((int) (red * percent), (int) (green * percent), (int) (blue * percent));
    };

    private static String getTotalDuration(List<Track> tracks) {

        StringBuilder sb = new StringBuilder();

        int minutes = 0, seconds = 0;

        for (int i=0; i<tracks.size(); i++) {
            String[] duration = tracks.get(i).getDuration().split(": ");
            minutes += Integer.parseInt(duration[0]);
            seconds += Integer.parseInt(duration[1]);
        }

        if (seconds >= 60) {
            minutes += (seconds/60);
            seconds = seconds % 60;
        }

        sb.append(minutes);
        sb.append(" minutes");
        if (seconds > 0) {
            sb.append(" ");
            sb.append(seconds);
            sb.append(" seconds");
        }

        return sb.toString();

    };

    private void setBackground() {

//        activity.getWindow().setStatusBarColor(context.getColor(R.color.black));
//        main.setBackgroundColor(context.getColor(R.color.black));

        String hexColor = convert(album.getColor());
        int hexColor_orig = convert(album.getColor(), 0.8);
        int hex_middle = convert(album.getColor(), 0.5);
        int black = convert("rgba(0,0,0,1)", 0.5);

        int[] initial_colors = {
//                Color.parseColor("#000000"),
//                Color.parseColor("#000000")
                black,
                black
        };
        GradientDrawable initial = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, initial_colors);

        int[] colors = {
//                Color.parseColor(hexColor),
//                Color.parseColor("#000000")
                hexColor_orig,
                hex_middle,
                black
        };

        GradientDrawable gd_main = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
//        main.setBackground(gd_main);

//        activity.getWindow().setStatusBarColor(Color.parseColor(hexColor));
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);

        GradientDrawable[] trans_colors = new GradientDrawable[] {initial, gd_main};
        TransitionDrawable trans = new TransitionDrawable(trans_colors);
        main.setBackground(trans);
        trans.startTransition(300);

    };

    private void onBackPress(boolean vibrate) {
        if (album == null) return;
        if (vibrate) Common.vibrate(getContext(), 50);
        closeFragment();
    };

    private void openAlbumMenu() {

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
                new BottomMenu.MenuItem(null, "Play Next", playNext),
                new BottomMenu.MenuItem(null, "Add To Queue", addToQueue),
                new BottomMenu.MenuItem(null, "Share Album", share)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(context)
                .setDataList(menu_list)
                .setName(album.getAlbum())
                .setArtist(album.getAlbumArtist())
                .setThumbnail(album.getThumbnail())
                .build();

        bottomMenu_new.show();

    };

    private void openTrackMenu(int position) {

        Track track = album.getTracks().get(position);

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
                new BottomMenu.MenuItem(null, "Play Next", playNext),
                new BottomMenu.MenuItem(null, "Add To Queue", addToQueue),
                new BottomMenu.MenuItem(null, "Share Track", share)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
                .setContext(context)
                .setDataList(menu_list)
                .setName(track.getTitle())
                .setArtist(track.getArtist())
                .setThumbnail(track.getThumbnail())
                .build();

        bottomMenu_new.show();

    };

    private void playAndAddAlbumToQueue(boolean should_vibrate) {

        if (should_vibrate) Common.vibrate(getContext(), 50);

        List<Track> tracks = album.getTracks();

        MusicForegroundService.NowPlayingData.getInstance(getContext())
            .setNowPlayingTrack(tracks.get(0));
//            .commit();

        MusicService.start(getContext());
        if (tracks.size() < 2) return;
        for (int i=1; i<tracks.size(); i++) {
            MusicForegroundService.NowPlayingData.getInstance(getContext())
                .addToQueue(tracks.get(i));
        }

    };

    private void pauseAlbum() {
        Common.vibrate(getContext(), 50);
        MusicService.pause();
    };

    private void updateTrack() {
        if (album == null) return;
        if (adapter == null) return;
        adapter.notifyDataSetChanged();
    };

    public boolean isNowPlayingSameAlbum() {
        if (album == null) return false;
        Track now_playing = MusicForegroundService.NowPlayingData.getInstance(getContext()).getTrack();
        if (now_playing == null) return false;
        return now_playing.getAlbumId().equals(album.getAlbumId());
    };

};