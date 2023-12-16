package com.app.studiomusic.Audio_Controller;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

public class MusicApplication extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "now_playing";
    public static final String TRACK_CHANGE = "track_change";
    public static final String PLAY_PAUSE = "play/pause";
    public static final String PLAYBACK_STOP = "playback_stop";
    public static final String QUEUE_TRACK_SWAP = "queue_track_swap";
    public static final String TRACK_REMOVE = "track_remove";
    public static final String RADIO_RESPONSE = "radio_response";

    public static enum MORE_SEARCH_TYPES {
        TRACKS, ALBUMS
    };

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d("servicelog","app on create");

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Now Playing", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("Shows a notification of the current media session");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

    };

    @Override
    public void onTerminate() {
        Log.d("servicelog", "app on terminate");
        super.onTerminate();
    }

}
