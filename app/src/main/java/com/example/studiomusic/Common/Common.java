package com.example.studiomusic.Common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.studiomusic.API_Controller.APIService;
import com.example.studiomusic.Audio_Controller.MusicService;
import com.example.studiomusic.Loader;
import com.example.studiomusic.LoginActivity;
import com.example.studiomusic.SP_Controller.SPService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class Common {

    private static void signOutResponse(JSONObject res, Context context, Loader loader) {

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient signInClient = GoogleSignIn.getClient(context, gso);
        signInClient.revokeAccess();

        SharedPreferences user = SPService.USER(context);
        SharedPreferences.Editor editor = user.edit();
        editor.clear();
        editor.apply();
        SharedPreferences token = SPService.TOKEN(context);
        editor = token.edit();
        editor.clear();
        editor.apply();

        if (loader != null) loader.stopLoading();
        context.startActivity(new Intent(context, LoginActivity.class));
        ((Activity) context).finishAffinity();
//        MusicService.getInstance(context).stopService();
        MusicService.getInstance(context.getApplicationContext()).release();

    }

    private static void signOutError(VolleyError err, Context context, Loader loader) {
        Toast.makeText(context, "ERROR!", Toast.LENGTH_SHORT).show();
        if (loader != null) loader.stopLoading();
    }

    public static void signOut(Context context, Loader loader) {

        String userId = SPService.USER(context).getString("_id",null);
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "JSONObject error!", Toast.LENGTH_SHORT).show();
            return;
        }

        APIService.signOut(
                context,
                obj.toString(),
                res -> signOutResponse(res, context, loader),
                err -> signOutError(err, context, loader)
        );

    }

}
