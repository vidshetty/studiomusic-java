package com.example.studiomusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.SP_Controller.SPService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

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
    private Vibrator vibrator = null;

    String type = null, period = null,
            username = null, email = null, picture = null;
    Boolean seen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_loader);
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

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        responseButton = findViewById(R.id.profilecheck_continue);

        signOutButton = findViewById(R.id.profilecheck_signout);
        signOutButton.setOnClickListener(this::revoke);

        wholeActivity = findViewById(R.id.profilecheck_main);
        wholeActivity.setOnClickListener(view -> {
            if (view.getId() == R.id.username_edittext) return;
            usernameEdit.clearFocus();
        });

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
                    vibrator.vibrate(100);
                    callAPI();
                    break;
                case "signup":
                    vibrator.vibrate(100);
                    String text = usernameEdit.getText().toString();
                    usernameEdit.clearFocus();
                    if (text.equals("")) {
                        usernameEdit.setError("Enter username!");
                        return;
                    }
                    callAPI();
                case "expired":
                    vibrator.vibrate(100);
                    requestAccess();
            }
        });

    }

    private void accessCheckError(VolleyError err) {
        Toast.makeText(this, "accessCheck error!", Toast.LENGTH_SHORT).show();
    }

    private void revoke(View view) {
        vibrator.vibrate(100);
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

}