package com.app.studiomusic.AppUpdates;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.app.studiomusic.API_Controller.API;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.BuildConfig;
import com.app.studiomusic.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;


public class UpdateChecker {

    private static final String UPDATE_NAME = "studiomusic-update.apk";

    public static void checkUpdates(
            Context context,
            Response.Listener<JSONObject> responseListener,
            Response.ErrorListener errorListener
    ) {
        String queryString = "?" +
                "versionCode=" + BuildConfig.VERSION_CODE + "&" +
                "versionName=" + BuildConfig.VERSION_NAME + "&" +
                "buildType=" + BuildConfig.BUILD_TYPE;
        APIService.checkForUpdates(context, queryString, responseListener, errorListener);
    };

    public static void install(Context context) {

        try {

            String path = context.getFilesDir().toString() + "/" + UPDATE_NAME;

            File file = new File(path);
            if (!file.exists()) return;

            UpdateBottomMenu updateBottomMenu = new UpdateBottomMenu.Builder()
                    .setContext(context)
                    .setFilePath(path)
                    .build();

            updateBottomMenu.show();

        }
        catch(Exception e) {
            Log.d("update_checker", e.getMessage() + "");
        }

    };

    private static void sendDownloadCompleteBroadcast(Context context) {
        Intent intent = new Intent(MusicApplication.UPDATE_DOWNLOAD);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    };

    public static void downloadAndInstall(Context context, boolean should_install) {

        String path = context.getFilesDir().toString() + "/" + UPDATE_NAME;

        File file = new File(path);

        if (file.exists()) {

            if (should_install) sendDownloadCompleteBroadcast(context);

        }
        else {

            String queryString = "?buildType=" + BuildConfig.BUILD_TYPE;

            Request<byte[]> request = new Request<byte[]>(
                    Request.Method.GET,
                    context.getString(R.string.prod_server) + "/api/downloadLatestUpdate" + queryString,
                    (volleyError) -> {
                        Log.d("update_checker", "request error " + volleyError.getMessage());
                    }
            ) {

                @Override
                protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                    return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
                };

                @Override
                protected void deliverResponse(byte[] response) {
                    try {

                        FileOutputStream fileOutputStream = context.openFileOutput(UPDATE_NAME, MODE_PRIVATE);
                        fileOutputStream.write(response);
                        fileOutputStream.close();
//                        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show();

                        if (should_install) sendDownloadCompleteBroadcast(context);

                    }
                    catch (Exception e) {
                        Log.d("update_checker", e.getMessage() + " ");
//                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                    }
                };

            };

            new API(context).getQueue().add(request);

        }

    };

    public static void removeExistingApk(Context context) {
        String path = context.getFilesDir().toString() + "/" + UPDATE_NAME;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    };

    public static void initiateCompleteFlow(Context context) {

        checkUpdates(
            context,
            (response)-> {

                boolean updatesAvailable = response.optBoolean("updateAvailable", true);
//                boolean updatesAvailable = true;

                Log.d("update_checker", "updates available " + updatesAvailable);

                if (!updatesAvailable) {
                    removeExistingApk(context);
                    return;
                }

                downloadAndInstall(context, true);

            },
            (err) -> {
                Log.d("update_checker", "err checking updates " + new String(err.networkResponse.data));
                return;
            }
        );

    };

};
