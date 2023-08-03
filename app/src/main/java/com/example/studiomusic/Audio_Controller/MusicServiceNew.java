package com.example.studiomusic.Audio_Controller;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class MusicServiceNew {

    private static final String tag = "musicservicenew_log";
    private static ServiceConnection serviceConnection = null;
    private static MusicForegroundService music_service = null;

    public static void start(Context context) {
        context.startForegroundService(new Intent(context, MusicForegroundService.class));
//        bindToService();
    };

    private static void bindToService() {
        if (serviceConnection != null) return;
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                music_service = ((MusicForegroundService.MusicForegroundServiceBinder) iBinder).getService();
            };
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                music_service = null;
            };
        };
    };

    public static void stop(Context context) {
        context.stopService(new Intent(context, MusicForegroundService.class));
    };

    public static void bind(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, MusicForegroundService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    };

    public static void unBind(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    };

    public static void pause() {
        if (music_service == null) return;
        music_service.pause_manual();
    };

    public static void play() {
        if (music_service == null) return;
        music_service.play_manual();
    };

    public static boolean isForegroundServiceRunning(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (MusicForegroundService.class.getName().equals(serviceInfo.service.getClassName())) {
                if (serviceInfo.foreground) return true;
            }
        }

        return false;

    };

};
