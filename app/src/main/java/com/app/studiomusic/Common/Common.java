package com.app.studiomusic.Common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.MusicData.Album;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.SP_Controller.SPService;
import com.app.studiomusic.Login.LoginActivity;
import com.app.studiomusic.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Common {

    private static void signOutResponse(JSONObject res, Context context, Loader loader) {

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient signInClient = GoogleSignIn.getClient(context, gso);
        signInClient.revokeAccess();

//        SignInClient signInClient = Identity.getSignInClient(context);
//        signInClient.signOut();

        SharedPreferences user = SPService.USER(context);
        SharedPreferences.Editor editor = user.edit();
        editor.clear();
        editor.apply();
        SharedPreferences token = SPService.TOKEN(context);
        editor = token.edit();
        editor.clear();
        editor.apply();
        SharedPreferences search_history = SPService.SEARCH_HISTORY(context);
        editor = search_history.edit();
        editor.clear();
        editor.apply();

        context.stopService(new Intent(context, MusicForegroundService.class));
        MusicForegroundService.NowPlayingData.getInstance(context).destroyInstance();

        if (loader != null) loader.stopLoading();
        context.startActivity(new Intent(context, LoginActivity.class));
        ((Activity) context).finishAffinity();

    };

    private static void signOutError(VolleyError err, Context context, Loader loader) {
        Toast.makeText(context, "ERROR!", Toast.LENGTH_SHORT).show();
        if (loader != null) loader.stopLoading();
    };

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
                err -> signOutResponse(null, context, loader)
//                err -> signOutError(err, context, loader)
        );

    };

    public static Track buildTrack(JSONObject track_data) {
        try {

            Track.Builder track_builder = new Track.Builder();

            track_builder.setTrackId(track_data.getString("_trackId"))
                    .setTitle(track_data.getString("Title"))
                    .setArtist(track_data.getString("Artist"))
                    .setDuration(track_data.getString("Duration"))
                    .setUrl(track_data.getString("url"))
                    .setLyrics(track_data.getBoolean("lyrics"))
                    .setSync(track_data.getBoolean("sync"))
                    .setAlbumId(track_data.getString("_albumId"))
                    .setAlbum(track_data.getString("Album"))
                    .setColor(track_data.getString("Color"))
                    .setReleaseDate(track_data.getString("releaseDate"))
                    .setThumbnail(track_data.getString("Thumbnail"))
                    .setYear(track_data.getString("Year"))
                    .setType(track_data.getString("Type"))
                    .setLightColor(track_data.optString("LightColor", null))
                    .setDarkColor(track_data.optString("DarkColor", null));

            return track_builder.build();

        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    public static Album buildAlbum(JSONObject data) {
        try {

            JSONArray each_tracks = data.getJSONArray("Tracks");

            Album.Builder album_builder = new Album.Builder();

            album_builder.setAlbumId(data.getString("_albumId"))
                    .setAlbum(data.getString("Album"))
                    .setAlbumArtist(data.getString("AlbumArtist"))
                    .setType(data.getString("Type"))
                    .setYear(data.getString("Year"))
                    .setColor(data.getString("Color"))
                    .setThumbnail(data.getString("Thumbnail"))
                    .setReleaseDate(data.getString("releaseDate"))
                    .setLightColor(data.optString("LightColor", null))
                    .setDarkColor(data.optString("DarkColor", null));

            List<Track> trackList = new ArrayList<>();

            for (int j = 0; j < each_tracks.length(); j++) {
                JSONObject track = each_tracks.getJSONObject(j);
                track.put("_albumId", data.getString("_albumId"));
                track.put("Album", data.getString("Album"));
                track.put("Color", data.getString("Color"));
                track.put("LightColor", data.optString("LightColor", null));
                track.put("DarkColor", data.optString("DarkColor", null));
                track.put("Year", data.getString("Year"));
                track.put("Thumbnail", data.getString("Thumbnail"));
                track.put("releaseDate", data.getString("releaseDate"));
                track.put("Type", data.getString("Type"));
                trackList.add(buildTrack(track));
            }

            album_builder.setTracks(trackList);

            return album_builder.build();

        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    public static Intent shareAlbum(Album album) {

        if (album == null) return null;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String type = album.getType().toLowerCase();
        String text = "Listen to the " + type + " " + album.getAlbum() + " ";
        text += "by " + album.getAlbumArtist() + " ";
        text += "only on StudioMusic ";
        text += "https://studiomusic.app/player/album/" + album.getAlbumId();
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        return Intent.createChooser(sendIntent, null);

    };

    public static Intent shareTrack(Track track) {

        if (track == null) return null;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String text = "Listen to the track " + track.getTitle() + " ";
        text += "by " + track.getArtist() + " ";
        text += "only on StudioMusic ";
        text += "https://studiomusic.app/player/track/" + track.getAlbumId() + "/" + track.getTrackId();
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        return Intent.createChooser(sendIntent, null);

    };

    public static String convertStringWithAnd(String name) {
        if (name == null) return null;
        List<String> split = Arrays.asList(name.split(", "));
        if (split.size() == 1) return name;
        StringBuilder finalString = new StringBuilder();
        for (int i=0; i<split.size(); i++) {
            finalString.append(split.get(i));
            if (i < split.size()-2) finalString.append(", ");
            else if (i == split.size()-2) finalString.append(" & ");
            else continue;
        }
        return finalString.toString();
    };

    public static void vibrate(Context context, long ms) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else {
            vibrator.vibrate(ms);
        }
    };

    public static int convertDpToPixel(Context context, int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    };

    public static int getStatusBarHeight(Context context) {
        Resources myResources = context.getResources();
        int idStatusBarHeight = myResources.getIdentifier( "status_bar_height", "dimen", "android");
        if (idStatusBarHeight <= 0) return 0;
        return context.getResources().getDimensionPixelSize(idStatusBarHeight);
    };

    public static int getNavigationBarHeight(Context context) {
        Resources myResources = context.getResources();
        int idStatusBarHeight = myResources.getIdentifier( "navigation_bar_height", "dimen", "android");
        if (idStatusBarHeight <= 0) return 0;
        return context.getResources().getDimensionPixelSize(idStatusBarHeight);
    };

    public static void showSnackBar(Context context, View view, String text) {
        Snackbar bar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(context.getColor(R.color.white))
            .setDuration(1000)
            .setTextColor(context.getColor(R.color.black));
        bar.show();
    };

    public static int convert(String color, double percent) {
        String[] cut = color.substring(5,color.length()-1).split(",");
        int red = Integer.parseInt(cut[0]);
        int green = Integer.parseInt(cut[1]);
        int blue = Integer.parseInt(cut[2]);
        return Color.rgb((int) (red * percent), (int) (green * percent), (int) (blue * percent));
    };

};
