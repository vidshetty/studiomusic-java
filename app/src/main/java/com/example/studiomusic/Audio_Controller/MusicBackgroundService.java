package com.example.studiomusic.Audio_Controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.studiomusic.R;

import java.io.IOException;

public class MusicBackgroundService extends Service {

    private static final String tag = "servicelog";
    private static int REPEAT = 1;

    private MediaPlayer mp = null;
    private Track track = Track.getDetails();

    private MediaSession mediaSession = null;
    private Notification.MediaStyle mediaStyle = null;
    private NotificationManager notificationManager = null;
    private Notification.Builder notificationBuilder = null;
    private MediaMetadata.Builder metadataBuilder = null;

    public class MusicBackgroundServiceBinder extends Binder {
        public MusicBackgroundService getService() {
            return MusicBackgroundService.this;
        }
    }

    private MusicBackgroundServiceBinder binder = new MusicBackgroundServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(tag, "background service bind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "background service create");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mediaSession = new MediaSession(getApplicationContext(), "now_playing");
        mediaSession.setActive(true);
        mediaStyle = new Notification.MediaStyle().setMediaSession(mediaSession.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag, "background service start command");
        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(tag, "background service destroy");
        super.onDestroy();
    }

    public void release() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        if (mp == null) return;
        mp.release();
        mp = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(tag, "background service unbind");
        release();
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(tag, "service task remove");
        mediaSession.setActive(false);
        release();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void repeatFunc(MediaPlayer mediaPlayer) {
        if (REPEAT == 0) return;
        if (REPEAT == 1 || REPEAT == 2) {
            stop();
            play();
        }
    }

    private void createNotification() {

        notificationBuilder = new Notification.Builder(getApplicationContext(), MusicApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_studiomusic_white)
                .setStyle(mediaStyle)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setOnlyAlertOnce(true);

        metadataBuilder = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.album)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, track.albumArtist);
//                .putLong(MediaMetadata.METADATA_KEY_DURATION, mp.getDuration());

        Glide.with(getApplicationContext())
            .asBitmap()
            .load(track.thumbnail)
            .into(new CustomTarget<Bitmap>() {

                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, resource);
                    mediaSession.setMetadata(metadataBuilder.build());
                    notificationManager.notify(0, notificationBuilder.build());
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {}

            });

    }

    public void setUrl(String url, MediaPlayer.OnPreparedListener onPreparedListener) {
        if (mp == null) mp = new MediaPlayer();
        try {

            mp.setDataSource(track.url);
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    createNotification();
                    onPreparedListener.onPrepared(mediaPlayer);
                }
            });
            mp.setOnCompletionListener(this::repeatFunc);

            createNotification();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        Log.d(tag, "background service play");
        if (mp == null) return;
        mp.start();
    }

    public void pause() {
        Log.d(tag, "background service pause");
        if (!mp.isPlaying()) return;
        mp.pause();
    }

    public void stop() {
        Log.d(tag, "background service stop");
        if (mp.isPlaying()) mp.pause();
        mp.seekTo(0);
    }

    public boolean isPlaying() {
        if (mp == null) return false;
        return mp.isPlaying();
    }

}