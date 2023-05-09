package com.example.studiomusic.Audio_Controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.studiomusic.MusicData.Track_Test;
import com.example.studiomusic.R;

import java.io.IOException;

public class MusicBackgroundService extends Service {

    private static final String tag = "servicelog";
    private static int REPEAT = 1;

    private MediaPlayer mp = null;
    private Track_Test track = Track_Test.getDetails();

    private MediaSession mediaSession = null;
    private Notification.MediaStyle mediaStyle = null;
    private NotificationManager notificationManager = null;
    private Notification.Builder notificationBuilder = null;
    private MediaMetadata.Builder metadataBuilder = null;
    private AudioManager audioManager = null;
    private AudioAttributes audioAttributes = null;
    private AudioFocusRequest audioFocusRequest = null;

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int audioFocusChanged) {
            switch (audioFocusChanged) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    play();
                    break;
                default:
                    break;
            }
        }
    };

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
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag, "background service start command");
        return super.onStartCommand(intent, flags, startId);
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
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, track.albumArtist);

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
        release();
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setAudioAttributes(audioAttributes);
        }
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        Log.d(tag, "background service play");
        if (mp == null) return;
        int requestResult = audioManager.requestAudioFocus(audioFocusRequest);
        if (requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mp.start();
        }
    }

    public void pause() {
        Log.d(tag, "background service pause");
        if (!mp.isPlaying()) return;
        mp.pause();
        audioManager.abandonAudioFocusRequest(audioFocusRequest);
    }

    public void stop() {
        Log.d(tag, "background service stop");
        if (mp.isPlaying()) mp.pause();
        release();
    }

    public boolean isPlaying() {
        if (mp == null) return false;
        return mp.isPlaying();
    }

    public boolean isPrepared() {
        return mp != null;
    }

}