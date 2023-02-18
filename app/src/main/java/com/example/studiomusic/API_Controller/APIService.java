package com.example.studiomusic.API_Controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.Loader;
import com.example.studiomusic.LoginActivity;
import com.example.studiomusic.ProfileCheckActivity;
import com.example.studiomusic.R;
import com.example.studiomusic.SP_Controller.SPService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class APIService {

    private static void goToLogin(Context context) {
        Loader loader = new Loader((Activity) context);
        loader.startLoading();
        Common.signOut(context, loader);
        Toast.makeText(context, "Please log in!", Toast.LENGTH_SHORT).show();
    }

    private static void goToProfileCheck(Context context) {
        ((Activity) context).finishAffinity();
        context.startActivity(new Intent(context, ProfileCheckActivity.class));
    }

    public static void errorHandler(Context context, VolleyError err) {
        if (err.networkResponse == null) return;

        String statusCode = String.valueOf(err.networkResponse.statusCode);
        if (!statusCode.equals("500")) return;
        String body = new String(err.networkResponse.data, StandardCharsets.UTF_8);

        JSONObject obj = null;
        try {
            obj = new JSONObject(body);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Integer type = null;
        try {
            type = obj.getInt("errorType");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Boolean isMiddleware = null;
        try {
            isMiddleware = obj.getBoolean("middleware");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (!isMiddleware) return;

        if (type == 1) goToLogin(context);
        else if (type == 2) goToProfileCheck(context);
    }

    public static void checkServer(
            Context context,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {
        new API(context).JsonObjectRequest(
                Request.Method.GET,
                context.getString(R.string.prod_server) + "/api/checkServer",
                null,
                resListener,
                errorListener
        );
    }

    public static void accountCheck(
            Context context,
            String reqBody,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {
        new API(context).JsonObjectRequest(
                Request.Method.POST,
                context.getString(R.string.prod_server) + "/auth/accountCheck",
                reqBody,
                resListener,
                errorListener
        );
    }

    public static void accessCheck(
            Context context,
            String reqBody,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {
        new API(context).JsonObjectRequest(
                Request.Method.POST,
                context.getString(R.string.prod_server) + "/auth/accessCheck",
                reqBody,
                resListener,
                errorListener
        );
    }

    public static void continueLogIn(
            Context context,
            String reqBody,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {
        new API(context).JsonObjectRequest(
                Request.Method.POST,
                context.getString(R.string.prod_server) + "/auth/continueLogIn",
                reqBody,
                resListener,
                errorListener
        );
    }

    public static void signOut(
            Context context,
            String reqBody,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {
        new API(context).JsonObjectRequest(
                Request.Method.POST,
                context.getString(R.string.prod_server) + "/auth/signOut",
                reqBody,
                resListener,
                errorListener
        );
    }

    public static void requestAccess(
            Context context,
            String reqBody,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {
        new API(context).JsonObjectRequest(
                Request.Method.POST,
                context.getString(R.string.prod_server) + "/auth/requestAccess",
                reqBody,
                resListener,
                errorListener
        );
    }

}
