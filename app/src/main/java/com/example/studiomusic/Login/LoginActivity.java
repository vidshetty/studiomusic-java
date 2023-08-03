package com.example.studiomusic.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.android.volley.VolleyError;
import com.example.studiomusic.API_Controller.API;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.Common.Loader;
import com.example.studiomusic.ProfileCheck.ProfileCheckActivity;
import com.example.studiomusic.R;
import com.example.studiomusic.SP_Controller.SPService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private static final String tag = "login_tag";

    private Loader loader = null;
    private GoogleSignInClient signInClient = null;
    private GoogleSignInAccount account = null;
    private final int RC_SIGN_IN = 1000;
    private ActivityResultLauncher<Intent> launcher = null;

    private void accountCheckResponse(JSONObject response) {

        Log.d(tag, response.toString());

        SharedPreferences token = SPService.TOKEN(this);
        SharedPreferences.Editor editor = token.edit();
        try {
            editor.putString("accessToken", response.getString("accessToken"));
            editor.putString("refreshToken", response.getString("refreshToken"));
            editor.apply();
            API.setHeaders(this);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Token shared preferences error!", Toast.LENGTH_LONG).show();
            loader.stopLoading();
            return;
        }

        SharedPreferences user = SPService.USER(this);
        editor = user.edit();
        try {
            editor.putString("_id", response.getString("_id"));
            editor.putString("name", response.getString("name"));
            editor.putString("email", response.getString("email"));
            editor.putString("picture", response.getString("picture"));
            editor.apply();
            API.setHeaders(this);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(tag, e.getMessage());
            Toast.makeText(this, "User shared preferences error!", Toast.LENGTH_LONG).show();
            loader.stopLoading();
            return;
        }

        loader.stopLoading();
//        Toast.makeText(this, "You are logged in " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ProfileCheckActivity.class));
        finish();

    };

    private void accountCheckError(VolleyError err) {
        Toast.makeText(this, "account check error!", Toast.LENGTH_LONG).show();
        loader.stopLoading();
    }

    private void accountCheck() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", account.getDisplayName());
            obj.put("email", account.getEmail());
            obj.put("id", account.getId());
            obj.put("photoUrl", account.getPhotoUrl() == null ? JSONObject.NULL : account.getPhotoUrl());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSONObject error!", Toast.LENGTH_LONG).show();
            loader.stopLoading();
            return;
        }

        APIService.accountCheck(this, obj.toString(), this::accountCheckResponse, this::accountCheckError);

    }

    public void onResult(ActivityResult result) {

//        if (result.getResultCode() != RC_SIGN_IN) return;
        if (result.getData() == null) return;

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
        loader.startLoading();

        try {
            account = task.getResult(ApiException.class);
        }
        catch(ApiException err) {
            Toast.makeText(this, "Error signing in!", Toast.LENGTH_LONG).show();
            loader.stopLoading();
            return;
        }

        accountCheck();

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    onResult(result);
                }
            }
        );

        loader = new Loader(this);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(this::buttonClick);

        Button linkedIn = findViewById(R.id.linkedin);
        linkedIn.setOnClickListener(view -> {
            Common.vibrate(getApplicationContext(), 100);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.linkedin_uri))));
        });

    };

    private void buttonClick(View view) {
        Common.vibrate(getApplicationContext(), 100);
        Intent signInIntent = signInClient.getSignInIntent();
        launcher.launch(signInIntent);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
    };

}