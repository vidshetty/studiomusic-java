package com.example.studiomusic.Audio_Controller;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.util.Log;

public class MusicService {

    private static final String tag = "musicservicelog";

    private static MusicService instance = null;
    private static Context context = null;
    private static MusicBackgroundService musicService = null;
    private static boolean connectionMade = false;

    private static ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicBackgroundService.MusicBackgroundServiceBinder binder =
                    (MusicBackgroundService.MusicBackgroundServiceBinder) iBinder;
            musicService = binder.getService();
            connectionMade = true;
            Log.d(tag, "service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(tag, "service disconnected");
            connectionMade = false;
        }

    };

    private MusicService(Context context) {
        MusicService.context = context;
    }

    public static MusicService getInstance(Context context) {
        if (instance == null) instance = new MusicService(context);
        return instance;
    }

    public void startService() {
        Log.d(tag, "start service called");
        Log.d(tag, "" + connectionMade);
        if (connectionMade) return;
        Intent intent = new Intent(context, MusicBackgroundService.class);
        context.startService(intent);
    }

    public void bindService() {
        Log.d(tag, "bind service called");
        Log.d(tag, "" + connectionMade);
        if (connectionMade) return;
        Intent intent = new Intent(context, MusicBackgroundService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unBindService() {
        Log.d(tag, "unbind service called");
        if (!connectionMade) return;
        context.unbindService(serviceConnection);
    }

    public void setUrl(String url, MediaPlayer.OnPreparedListener onPreparedListener) {
        if (musicService == null) return;
        musicService.setUrl(url, onPreparedListener);
    }

    public void play() {
        if (musicService == null) return;
        musicService.play();
    }

    public void pause() {
        if (musicService == null) return;
        musicService.pause();
    }

    public void stop() {
        if (musicService == null) return;
        musicService.stop();
    }

    public void release() {
        if (musicService == null) return;
        musicService.release();
    }

    public boolean isPlaying() {
        if (musicService == null) return false;
        return musicService.isPlaying();
    }

//    @Override
//    protected void onDestroy() {
//        Log.d("servicelog","music service class destroy");
//        if (musicService != null) {
//            musicService.onDestroy();
//        }
//        super.onDestroy();
//    }

}