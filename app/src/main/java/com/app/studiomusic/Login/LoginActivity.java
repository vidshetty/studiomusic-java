package com.app.studiomusic.Login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.API;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.AppUpdates.UpdateChecker;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Common.Common;
import com.app.studiomusic.Common.Loader;
import com.app.studiomusic.ProfileCheck.ProfileCheckActivity;
import com.app.studiomusic.SP_Controller.SPService;
import com.app.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private static final String tag = "login_tag";

//    private SignInClient oneTapClient = null;
//    private BeginSignInRequest signInRequest = null;

    private GoogleSignInClient signInClient = null;
    private GoogleSignInAccount account = null;
    private Loader loader = null;
    private UpdateReceiver updateReceiver = null;
    private ActivityResultLauncher<Intent> launcher = null;
//    private ActivityResultLauncher<IntentSenderRequest> one_tap_launcher = null;

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateChecker.install(LoginActivity.this);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        IntentFilter intentFilter = new IntentFilter(MusicApplication.UPDATE_DOWNLOAD);
        if (updateReceiver == null) updateReceiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter);

        loader = new Loader(this);

        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    onResult(result);
                };
            }
        );

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);

//        one_tap_launcher = registerForActivityResult(
//            new ActivityResultContracts.StartIntentSenderForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    onResult(result);
//                };
//            }
//        );
//
//        oneTapClient = Identity.getSignInClient(this);
//
//        signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    .setServerClientId(getString(R.string.google_web_client_id))
//                    .setFilterByAuthorizedAccounts(false)
//                    .build()
//            )
//            .setAutoSelectEnabled(false)
//            .build();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(this::buttonClick);

        Button linkedIn = findViewById(R.id.linkedin);
        linkedIn.setOnClickListener(view -> {
            Common.vibrate(getApplicationContext(), 50);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.linkedin_uri))));
        });

    };

    private void buttonClick(View view) {

        Common.vibrate(this, 10);
        Intent signInIntent = signInClient.getSignInIntent();
        launcher.launch(signInIntent);

//        oneTapClient.beginSignIn(signInRequest)
//            .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
//                @Override
//                public void onSuccess(BeginSignInResult result) {
//                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
//                    one_tap_launcher.launch(intentSenderRequest);
//                };
//            })
//            .addOnFailureListener(this, new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d(tag, e.getLocalizedMessage() + " here");
//                };
//            });

    };

    private void accountCheckResponse(JSONObject response) {

        loader.stopLoading();

        SharedPreferences token = SPService.TOKEN(this);
        SharedPreferences.Editor token_editor = token.edit();
        try {
            token_editor.putString("accessToken", response.getString("accessToken"));
            token_editor.putString("refreshToken", response.getString("refreshToken"));
            token_editor.apply();
            API.setHeaders(this);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Token shared preferences error!", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences user = SPService.USER(this);
        SharedPreferences.Editor user_editor = user.edit();
        try {
            user_editor.putString("_id", response.getString("_id"));
            user_editor.putString("name", response.getString("name"));
            user_editor.putString("email", response.getString("email"));
            String picture = response.isNull("picture") ? null : response.getString("picture");
            user_editor.putString("picture", picture);
            user_editor.apply();
            API.setHeaders(this);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(tag, e.getMessage() + " ");
            Toast.makeText(this, "User shared preferences error!", Toast.LENGTH_LONG).show();
            return;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);

        startActivity(new Intent(this, ProfileCheckActivity.class));
        finish();

    };

    private void accountCheckError(VolleyError err) {
        Toast.makeText(this, "account check error!", Toast.LENGTH_LONG).show();
        Log.d(tag, Objects.requireNonNull(err.getMessage()));
        loader.stopLoading();
    }

    public void onResult(ActivityResult result) {

        try {

            if (result.getData() == null) return;

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

            account = task.getResult(ApiException.class);

            JSONObject obj = new JSONObject();
            obj.put("name", account.getDisplayName());
            obj.put("email", account.getEmail());
            obj.put("id", account.getId());
            obj.put("photoUrl", account.getPhotoUrl() == null ? JSONObject.NULL : account.getPhotoUrl().toString());

            loader.startLoading();

            APIService.accountCheck(this, obj.toString(), this::accountCheckResponse, this::accountCheckError);

        } catch (ApiException err) {
            err.printStackTrace();
            Toast.makeText(this, "Error signing in!", Toast.LENGTH_LONG).show();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSONObject error!", Toast.LENGTH_LONG).show();
        }

//        try {
//
//            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
//
//            String google_id_token = credential.getGoogleIdToken();
//            String email = credential.getId();
//            String password = credential.getPassword();
//            String display_name = credential.getDisplayName();
//            String photo_url = credential.getProfilePictureUri() == null ? null : credential.getProfilePictureUri().toString();
//
//            Log.d(tag, "google_id_token " + google_id_token);
//            Log.d(tag, "email " + email);
//            Log.d(tag, "password " + password);
//            Log.d(tag, "display_name " + display_name);
//            Log.d(tag, "photo_url " + credential.getProfilePictureUri());
//
//            JSONObject obj = new JSONObject();
//            obj.put("name", display_name);
//            obj.put("email", email);
//            obj.put("id", google_id_token);
//            obj.put("photoUrl", photo_url == null ? JSONObject.NULL : photo_url);
//
//            //            APIService.accountCheck(this, obj.toString(), this::accountCheckResponse, this::accountCheckError);
//
//        } catch (ApiException e) {
//            Log.d(tag, "on result error " + e.getMessage());
//            Log.d(tag, e.getLocalizedMessage() + "");
//            return;
//        } catch (JSONException e) {
//            Log.d(tag, "json error " + e.getMessage());
//            return;
//        }

    };

};