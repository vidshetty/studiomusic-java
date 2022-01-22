package com.example.studiomusic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {

    private GoogleSignInAccount account;
    private boolean accountExists = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = "https://studiomusic.herokuapp.com/api/activateCheck";
        RequestQueue queue = RequestSingleton.getInstance(this).getRequestQueue();
        call(url, queue);
    }

    private void call(String url, RequestQueue queue) {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        response(res);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseError(error, url, queue);
                    }
                }
        );
        queue.add(req);
    }

    private void response(JSONObject response) {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) accountExists = true;
        if (accountExists) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    private void responseError(VolleyError err, String url, RequestQueue q) {
        Toast.makeText(this, "Error connecting to server. Retrying...", Toast.LENGTH_SHORT).show();
        call(url, q);
    }

}
