package com.example.studiomusic.Main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.studiomusic.Common.ProfileImage;
import com.example.studiomusic.FragHomescreen.Fragment_Homescreen;
import com.example.studiomusic.FragLibrary.Fragment_Library;
import com.example.studiomusic.FragSearch.Fragment_Search;
import com.example.studiomusic.Profile.ProfileActivity;
import com.example.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "lifecycle";

    private GoogleSignInAccount account = null;
    private Vibrator vibrator = null;
    private BottomNavigationView navigationView = null;
    private ImageView search_clear = null;
    private EditText search_edittext = null;
    private Fragment_Homescreen frag_home = new Fragment_Homescreen();
    private Fragment_Search frag_search = new Fragment_Search();
    private Fragment_Library frag_library = new Fragment_Library();

    private Toolbar toolBar = null;
    private RelativeLayout mainRelativeLayout = null;
    private ConstraintLayout searchRelativeLayout = null;

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
        search_clear = findViewById(R.id.search_clear);
        search_edittext = findViewById(R.id.search_edittext);

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
                getWindow().setStatusBarColor(getColor(R.color.light_black));
            }
            else {
                mainRelativeLayout.setVisibility(View.VISIBLE);
                searchRelativeLayout.setVisibility(View.GONE);
                getWindow().setStatusBarColor(getColor(R.color.black));
            }
            setFragment(frag);
            return true;
        });

        search_edittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            };

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() < 1) search_clear.setVisibility(View.GONE);
                else search_clear.setVisibility(View.VISIBLE);
            };

            @Override
            public void afterTextChanged(Editable editable) {

            };

        });

        search_clear.setOnClickListener(view -> {
            search_edittext.setText("");
        });

    };

    private Fragment getFragment(int id) {
        if (id == R.id.homescreen) return frag_home;
        if (id == R.id.search) return frag_search;
        if (id == R.id.library) return frag_library;
        return null;
    };

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .commit();
    };

    private void setProfileImage() {

        ImageView profileImage = findViewById(R.id.profileImage);
        Glide.with(this)
                .load(account.getPhotoUrl() == null ? ProfileImage.getAlternativePicture(account.getDisplayName()) : account.getPhotoUrl())
                .into(profileImage);
        profileImage.setOnClickListener(view -> {
            vibrator.vibrate(50);
            startActivity(new Intent(this, ProfileActivity.class));
        });

    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            EditText et = findViewById(R.id.search_edittext);
            ImageView clear = findViewById(R.id.search_clear);
            Rect etRect = new Rect();
            et.getGlobalVisibleRect(etRect);
            Rect clearRect = new Rect();
            clear.getGlobalVisibleRect(clearRect);
            if (
                    !(etRect.contains((int)event.getRawX(), (int)event.getRawY()) ||
                    clearRect.contains((int)event.getRawX(), (int)event.getRawY()))
            ) {
                et.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            }

//            View v = getCurrentFocus();
//            Log.d("instanceof", (v instanceof EditText) + "");
//            Log.d("instanceof", (v instanceof LinearLayout) + "");
//            if (v != null) Log.d("instanceof", getResources().getResourceName(v.getId()));
////            if (v != null) Log.d("instanceof", (v.getId() == R.id.search_toolbar) + " id check");
////            if (v != null) Log.d("instanceof", (v.getId() == R.id.search_edittext) + " id check");
//            if (v instanceof EditText) {
//                Rect outRect = new Rect();
//                v.getGlobalVisibleRect(outRect);
//                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
//                    v.clearFocus();
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
//            }

        }
        return super.dispatchTouchEvent(event);
    };

};
