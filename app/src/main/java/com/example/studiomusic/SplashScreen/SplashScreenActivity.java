package com.example.studiomusic.SplashScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.android.volley.VolleyError;
import com.example.studiomusic.API_Controller.API;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.Login.LoginActivity;
import com.example.studiomusic.Main.MainActivity;
import com.example.studiomusic.ProfileCheck.ProfileCheckActivity;
import com.example.studiomusic.SP_Controller.SPService;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {

    private int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);

        View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                return false;
            }
        });

        super.onCreate(savedInstanceState);

//        callAPI();
        response(null);

    };

    private boolean userExists() {
        SharedPreferences user = SPService.USER(this);
        SharedPreferences tokens = SPService.TOKEN(this);
        return user.getString("_id",null) != null && user.getString("name",null) != null &&
                user.getString("email",null) != null && user.getString("picture",null) != null &&
                tokens.getString("accessToken",null) != null &&
                tokens.getString("refreshToken",null) != null;
    };

    private void callAPI() {
        if (count >= 2) {
            Toast.makeText(this, "Sorry, servers seem to be down!", Toast.LENGTH_LONG).show();
            return;
        }
        count++;
        APIService.checkServer(this, this::response, this::responseError);
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
    };

    @Override
    protected void onStop() {
        super.onStop();
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    };

    private void response(JSONObject response) {
        if (userExists()) {
            API.setHeaders(this);
            accessCheck();
        }
        else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    };

    private void responseError(VolleyError err) {
        Toast.makeText(this, "Error connecting to server. Retrying...", Toast.LENGTH_SHORT).show();
        callAPI();
    };

    private void accessCheck() {
        JSONObject obj = new JSONObject();
        String userId = SPService.USER(this).getString("_id", null);
        try {
            obj.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON error!", Toast.LENGTH_SHORT).show();
            return;
        }
        APIService.accessCheck(this, obj.toString(), this::accessCheckResponse, this::accessCheckError);
    };

    private void accessCheckResponse(JSONObject response) {

        // type -> [expired, revoked, signup, login]
        // period -> string
        // user -> name, email, picture, _id
        // seen -> boolean or null
        String type = null;
        Boolean seen = null;
        try {
            type = response.getString("type");
            seen = response.isNull("seen") ? null : response.getBoolean("seen");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSON parsing error!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.equals("expired") || type.equals("revoked") || (seen != null && !seen))
            startActivity(new Intent(this, ProfileCheckActivity.class));
        else startActivity(new Intent(this, MainActivity.class));
        finish();

    };

    private void accessCheckError(VolleyError err) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    };

}