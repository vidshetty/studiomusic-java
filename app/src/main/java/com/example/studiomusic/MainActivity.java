package com.example.studiomusic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.studiomusic.Audio_Controller.MusicService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "lifecycle";

    private GoogleSignInAccount account = null;
    private Vibrator vibrator = null;
    private BottomNavigationView navigationView = null;
    private Fragment_Homescreen frag_home = new Fragment_Homescreen();
    private Fragment_Search frag_search = new Fragment_Search();
    private Fragment_Library frag_library = new Fragment_Library();

    private Toolbar toolBar = null;
    private RelativeLayout mainRelativeLayout = null;
    private RelativeLayout searchRelativeLayout = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().setNavigationBarColor(getColor(R.color.light_black));

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        account = GoogleSignIn.getLastSignedInAccount(this);
        setProfileImage();

        navigationView = findViewById(R.id.bottom_nav_bar);

        toolBar = findViewById(R.id.main_toolbar);
        mainRelativeLayout = findViewById(R.id.toolbar_layout);
        searchRelativeLayout = findViewById(R.id.search_toolbar);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_frame_layout, frag_home)
            .commit();

        navigationView.setOnItemSelectedListener(item -> {
            Fragment frag = getFragment(item.getItemId());
            if (frag == null) return false;
            if (item.getItemId() == R.id.search) {
                mainRelativeLayout.setVisibility(View.GONE);
                searchRelativeLayout.setVisibility(View.VISIBLE);
            }
            else {
                mainRelativeLayout.setVisibility(View.VISIBLE);
                searchRelativeLayout.setVisibility(View.GONE);
            }
            setFragment(frag);
            return true;
        });

    }

    @Override
    protected void onDestroy() {
        Log.d("servicelog", "main activity destroy");
        super.onDestroy();
    }

    private Fragment getFragment(int id) {
        if (id == R.id.homescreen) return frag_home;
        if (id == R.id.search) return frag_search;
        if (id == R.id.library) return frag_library;
        return null;
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .commit();
    }

    private void setProfileImage() {

        ImageView profileImage = findViewById(R.id.profileImage);
        Glide.with(this)
                .load(account.getPhotoUrl() == null ? ProfileImage.getAlternativePicture(account.getDisplayName()) : account.getPhotoUrl())
                .into(profileImage);
        profileImage.setOnClickListener(view -> {
            vibrator.vibrate(50);
            startActivity(new Intent(this, ProfileActivity.class));
        });

    }

}
