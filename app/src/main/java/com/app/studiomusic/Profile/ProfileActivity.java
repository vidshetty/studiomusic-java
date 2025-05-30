package com.app.studiomusic.Profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.AppUpdates.UpdateChecker;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.SP_Controller.SPService;
import com.bumptech.glide.Glide;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.Loader;
import com.app.studiomusic.Common.MultipleRequests;
import com.app.studiomusic.Common.ProfileImage;
import com.app.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    private MultipleRequests mulreq = null;

    private Loader loader = new Loader(this);

    private ImageView backButton = null;
    private Button signOut = null;
    private ImageView profileImage = null;
    private TextView profileUsername = null, profileEmail = null;

    private ConstraintLayout fullScreenLoader = null;
    private ConstraintLayout fullScreenContent = null;
    private ConstraintLayout periodTextContainer = null;
    private TextView periodText = null;
    private UpdateReceiver updateReceiver = null;

    private String type = null, period = null;
    private String username = null, email = null, picture = null;

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateChecker.install(ProfileActivity.this);
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mulreq = new MultipleRequests();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicApplication.UPDATE_DOWNLOAD);
        if (updateReceiver == null) updateReceiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter);

        fullScreenLoader = findViewById(R.id.profile_page_loader);
        fullScreenContent = findViewById(R.id.profile_page_content);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        Button signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(this::revoke);

        profileAPI();

    };

    private void profileAPIResponse(JSONObject response) {

        try {
            type = response.getString("type");
            period = response.getString("period");
            JSONObject user = response.getJSONObject("user");
            username = user.isNull("name") ? null : user.getString("name");
            email = user.getString("email");
            picture = user.isNull("picture") ? null : user.getString("picture");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON parsing error!", Toast.LENGTH_SHORT).show();
            finish();
        }

        fullScreenLoader.setVisibility(View.GONE);
        fullScreenContent.setVisibility(View.VISIBLE);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        profileImage = findViewById(R.id.profile_image);
        Glide.with(this).load(picture == null ? ProfileImage.getAlternativePicture(username) : picture).into(profileImage);

        profileUsername = findViewById(R.id.profile_username);
        profileUsername.setText(username == null ? account.getDisplayName() : username);
        profileEmail = findViewById(R.id.profile_email);
        profileEmail.setText(email);
        periodTextContainer = findViewById(R.id.profile_period_container);
        periodText = findViewById(R.id.profile_period);

        switch (type) {
            case "login":
            case "signup":
                periodText.setText("You have access for " + period);
                periodTextContainer.setBackground(getDrawable(R.drawable.profilecheck_periodtext_green));
                break;
            case "expired":
                periodText.setText("Your access has expired");
                periodTextContainer.setBackground(getDrawable(R.drawable.profilecheck_periodtext_red));
                break;
            case "revoked":
                periodText.setText("Your access has been revoked");
                periodTextContainer.setBackground(getDrawable(R.drawable.profilecheck_periodtext_red));
                break;
        }

    }

    private void profileAPIError(VolleyError err) {
        Toast.makeText(this, "Error loading profile details!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void profileAPI() {

        JSONObject obj = new JSONObject();
        SharedPreferences user = SPService.USER(this);
        try {
            obj.put("user_id", user.getString("_id", null));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIService.accessCheck(this, obj.toString(), this::profileAPIResponse, this::profileAPIError);

    }

    private void revoke(View view) {
        Common.vibrate(this, 50);
        loader.startLoading();
        Common.signOut(this, loader);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
    };
};
