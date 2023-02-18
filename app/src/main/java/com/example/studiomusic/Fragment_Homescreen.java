package com.example.studiomusic;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studiomusic.Audio_Controller.MusicService;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Homescreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Homescreen extends Fragment {

    private MusicService musicService = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ConstraintLayout fullScreen = null;
    private ConstraintLayout content = null;

    public Fragment_Homescreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Homescreen.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Homescreen newInstance(String param1, String param2) {
        Fragment_Homescreen fragment = new Fragment_Homescreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicService = MusicService.getInstance(getActivity().getApplicationContext());
        start();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__homescreen, container, false);
        fullScreen = view.findViewById(R.id.homescreen_fragment_loader);
        content = view.findViewById(R.id.homescreen_content);
        fullScreen.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
        new Handler().postDelayed(() -> {
            fullScreen.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }, 1000);
        return view;
    }

    private void start() {
        Log.d("servicelog", "music is playing " + musicService.isPlaying());
        if (musicService.isPlaying()) return;
        musicService.setUrl(
                "",
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        musicService.play();
                    }
                }
        );
    }
}