package com.app.studiomusic.ProfileCheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.app.studiomusic.Common.ProfileImage;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileCheckActivity extends AppCompatActivity {

    private final Loader loader = new Loader(this);
    private ImageView profileImage = null;
    private TextView profileUsername = null, profileEmail = null;
    private TextView periodText = null;
    private ConstraintLayout periodTextContainer = null;
    private EditText usernameEdit = null;
    private View wholeActivity = null;
    private Button responseButton = null;
    private Button signOutButton = null;
    private UpdateReceiver updateReceiver = null;

    String type = null, period = null, username = null, email = null, picture = null;
    Boolean seen = null;

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateChecker.install(ProfileCheckActivity.this);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_loader);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicApplication.UPDATE_DOWNLOAD);
        if (updateReceiver == null) updateReceiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter);

        accessCheck();

    }

    private void accessCheck() {
        JSONObject obj = new JSONObject();
        String userId = SPService.USER(this).getString("_id", null);
        try {
            obj.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        APIService.accessCheck(this, obj.toString(), this::accessCheckResponse, this::accessCheckError);
    }

    private void accessCheckResponse(JSONObject response) {

        // type -> [expired, revoked, signup, login]
        // period -> string
        // user -> name, email, picture, _id
        // seen -> boolean or null
        try {
            type = response.getString("type");
            period = response.getString("period");
            seen = response.isNull("seen") ? null : response.getBoolean("seen");
            JSONObject user = response.getJSONObject("user");
            username = user.isNull("name") ? null : user.getString("name");
            email = user.getString("email");
            picture = user.isNull("picture") ? null : user.getString("picture");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON parsing error!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setContentView(R.layout.activity_profile_check);

        responseButton = findViewById(R.id.profilecheck_continue);

        signOutButton = findViewById(R.id.profilecheck_signout);
        signOutButton.setOnClickListener(this::revoke);

        wholeActivity = findViewById(R.id.profilecheck_main);

        profileImage = findViewById(R.id.profileCheckImage);
        Glide.with(this).load(picture == null ? ProfileImage.getAlternativePicture(username) : picture).into(profileImage);

        usernameEdit = findViewById(R.id.username_edittext);
        usernameEdit.setOnFocusChangeListener((view, focus) -> {
            GradientDrawable shape = (GradientDrawable) usernameEdit.getBackground();
            if (focus) shape.setStroke(3, getColor(R.color.white));
            else shape.setStroke(3, getColor(R.color.light_white));
            usernameEdit.setCursorVisible(focus);
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        profileUsername = findViewById(R.id.profileUsername);
        profileUsername.setText(username == null ? account.getDisplayName() : username);
        profileEmail = findViewById(R.id.profileEmail);
        profileEmail.setText(email);

        periodTextContainer = findViewById(R.id.periodTextContainer);
        periodText = findViewById(R.id.periodText);

        switch (type) {
            case "login":
            case "signup":
                periodText.setText("You have access for " + period);
                periodTextContainer.setBackground(getDrawable(R.drawable.profilecheck_periodtext_green));
                if (type.equals("signup")) usernameEdit.setVisibility(View.VISIBLE);
                responseButton.setVisibility(View.VISIBLE);
                break;
            case "expired":
                periodText.setText("Your access has expired");
                periodTextContainer.setBackground(getDrawable(R.drawable.profilecheck_periodtext_red));
                responseButton.setVisibility(View.VISIBLE);
                responseButton.setText("Request access");
                break;
            case "revoked":
                periodText.setText("Your access has been revoked");
                periodTextContainer.setBackground(getDrawable(R.drawable.profilecheck_periodtext_red));
                responseButton.setVisibility(View.INVISIBLE);
                break;
        }

        responseButton.setOnClickListener(view -> {
            switch (type) {
                case "login":
                    Common.vibrate(this, 50);
                    callAPI();
                    break;
                case "signup":
                    Common.vibrate(this, 50);
                    String text = usernameEdit.getText().toString();
                    usernameEdit.clearFocus();
                    if (text.equals("")) {
                        usernameEdit.setError("Enter username!");
                        return;
                    }
                    callAPI();
                    break;
                case "expired":
                    Common.vibrate(this, 50);
                    requestAccess();
                    break;
            }
        });

    }

    private void accessCheckError(VolleyError err) {
        Toast.makeText(this, "accessCheck error!", Toast.LENGTH_SHORT).show();
    }

    private void revoke(View view) {
        Common.vibrate(this, 50);
        loader.startLoading();
        Common.signOut(this, loader);
    }

    private void callAPI() {
        JSONObject obj = new JSONObject();
        String userId = SPService.USER(this).getString("_id",null);
        String finalUsername = type.equals("signup") ? usernameEdit.getText().toString() : null;
        try {
            obj.put("user_id", userId);
            obj.put("username", finalUsername);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSONObject error!", Toast.LENGTH_SHORT).show();
            return;
        }
        loader.startLoading();
        APIService.continueLogIn(this, obj.toString(), this::continueLogInResponse, this::continueLogInError);
    }

    private void continueLogInResponse(JSONObject res) {
        loader.stopLoading();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void continueLogInError(VolleyError err) {
        APIService.errorHandler(this, err);
    }

    private void requestAccess() {
        loader.startLoading();
        APIService.requestAccess(
                this,
                null,
                view -> {
                    Toast.makeText(this, "Request has been sent!", Toast.LENGTH_SHORT).show();
                    loader.stopLoading();
                },
                error -> {
                    Toast.makeText(this, "Error sending request!", Toast.LENGTH_SHORT).show();
                    loader.stopLoading();
                }
        );
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}