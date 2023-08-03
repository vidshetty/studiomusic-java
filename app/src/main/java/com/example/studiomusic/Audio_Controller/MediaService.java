package com.example.studiomusic.Audio_Controller;

import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

public class MediaService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mediaSession = null;
    private PlaybackStateCompat playbackState = null;
    private NotificationManager notificationManager = null;
    private NotificationCompat.Builder notificationBuilder = null;
    private MediaMetadataCompat.Builder metadataBuilder = null;
    private AudioManager audioManager = null;
    private AudioAttributes audioAttributes = null;
    private AudioFocusRequest audioFocusRequest = null;
    private PlaybackStateCompat.Builder playbackStateBuilder = null;
    private MediaSessionCompat.Callback mediaSessionCallback = null;

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            super.onPlay();
        };
        @Override
        public void onPause() {
            super.onPause();
        };
        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        };
        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        };
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        };
    };

    @Override
    public void onCreate() {

        super.onCreate();

        mediaSession = new MediaSessionCompat(getApplicationContext(), "MEDIA_SESSION");

        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        playbackStateBuilder = new PlaybackStateCompat.Builder();

        playbackStateBuilder.setActions(
            PlaybackStateCompat.ACTION_PLAY_PAUSE |
            PlaybackStateCompat.ACTION_SEEK_TO |
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        );

        mediaSession.setPlaybackState(playbackStateBuilder.build());

        mediaSession.setCallback(new MediaSessionCallback());

        setSessionToken(mediaSession.getSessionToken());

    };

    @Override
    public void onLoadItem(String itemId, @NonNull Result<MediaBrowserCompat.MediaItem> result) {
        super.onLoadItem(itemId, result);
    };

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    };

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    };

};
