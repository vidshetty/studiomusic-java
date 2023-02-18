package com.example.studiomusic.API_Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.studiomusic.MainActivity;
import com.example.studiomusic.RequestSingleton;
import com.example.studiomusic.SP_Controller.SPService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class API {

    private static RequestQueue queue = null;
    private static Map<String,String> headers = new HashMap<>();

    public API(Context context) {
        queue = RequestSingleton.getInstance(context).getRequestQueue();
        if (headers.containsKey("authorization")) return;
        setHeaders(context);
    }

    public static void handleError(Context context) {
        Intent i = new Intent(context, MainActivity.class);
    }

    public static void setHeaders(Context context) {
        SharedPreferences token = SPService.TOKEN(context);
        String accessToken = token.getString("accessToken",null);
        String refreshToken = token.getString("refreshToken",null);
        if (accessToken == null || refreshToken == null) return;
        headers.put("accessToken",accessToken);
        headers.put("refreshToken",refreshToken);

        JSONObject deviceInfo = new JSONObject();
        try {
            deviceInfo.put("manufacturer", Build.MANUFACTURER);
            deviceInfo.put("model", Build.MODEL);
            deviceInfo.put("device", Build.DEVICE);
            deviceInfo.put("brand", Build.BRAND);
            headers.put("device-info", deviceInfo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void JsonObjectRequest(
            int method,
            String url,
            String reqBody,
            Response.Listener<JSONObject> resListener,
            Response.ErrorListener errorListener
    ) {

        JsonRequest<JSONObject> req = new JsonRequest<JSONObject>(
                method,
                url,
                reqBody,
                resListener,
                errorListener
        ) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String res = new String(response.data);
                    return Response.success(new JSONObject(res), null);
                }
                catch (NullPointerException | JSONException e) {
                    return Response.success(null, null);
                }
                catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

        };

        queue.add(req);

    }

    public void JsonArrayRequest(
            int method,
            String url,
            String reqBody,
            Response.Listener<JSONArray> resListener,
            Response.ErrorListener errorListener
    ) {

        JsonRequest<JSONArray> req = new JsonRequest<JSONArray>(
                method,
                url,
                reqBody,
                resListener,
                errorListener
        ) {

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String res = new String(response.data);
                    return Response.success(new JSONArray(res), null);
                }
                catch (NullPointerException | JSONException e) {
                    return Response.success(null, null);
                }
                catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

        };

        queue.add(req);

    }

    public void StringRequest(
            int method,
            String url,
            Response.Listener<String> resListener,
            Response.ErrorListener errorListener
    ) {

        StringRequest req = new StringRequest(
                method,
                url,
                resListener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        queue.add(req);

    }

}
